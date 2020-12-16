/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import external.openssl.*
import kotlinx.cinterop.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.tls.x509.X509Certificate
import tf.lotte.tinlok.system.BlockingResult
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.Closeable
import tf.lotte.tinlok.util.ClosedException

// Notes, in the order I discovered them:
// 1) OpenSSL 1.1 and 3.0 (the ones we use) does initialisation automatically. So we don't call
//    any init functions.
//    See: https://wiki.openssl.org/index.php/Library_Initialization


/**
 * An in-memory TLS processing object. This is used to handle TLS streams in-memory, separated away
 * from any sort of actual networking support, allowing easier composition of TLS-based protocols.
 *
 * Internally, this uses OpenSSL's memory BIOs to perform SSL encryption. The basic data flow can be
 * surmised as such:
 *
 *  - write(data) -> SSL encryption -> encrypted frames -> outgoing(data)
 *  - incoming(data) -> SSL decryption -> decrypted application data -> read(data)
 *
 * Sometimes, the TLS object either needs more data than has been provided, or needs to write to the
 * underlying stream. When this happens, the appropriate method will return a [BlockingResult]
 * with either the -1 or the -2 special constants (needs read and needs write, respectively).
 *
 * This object contains several properties to access certain attributes of the TLS connection. Doing
 * this before the handshake has completed is illegal and will throw an exception.
 *
 * The config object provided is used to configure OpenSSL's connection behaviour. The hostname
 * provided is used for TLS peer name verification.
 *
 * This class is @Unsafe as it contains references to externally allocated objects, which are not
 * automatically cleaned up.
 */
@Suppress("UNNECESSARY_LATEINIT")
@Unsafe
public actual class TlsObject actual constructor(
    public actual val context: TlsContext, private val hostname: String,
) : Closeable {
    /** Boolean to prevent double-frees. */
    private val isOpen = AtomicBoolean(true)

    // cached peer certificate, retreived from the context at handshake conclusion
    private var _peerCertificate: X509Certificate? = null

    // actual ssl object
    private lateinit var ssl: CPointer<SSL>

    // BIOs
    // Incoming data, written to by incoming() and read from with SSL_read()
    private lateinit var incomingBio: CPointer<BIO>

    // Outgoing data, written to by SSL_write() and read from with outgoing()
    private lateinit var outgoingBio: CPointer<BIO>

    init {
        try {
            ssl = context.SSL_new()

            // both BIOs are set to BIO_CLOSE to ensure they free their underlying memory
            incomingBio = BIO_new(BIO_s_mem()) ?: error("Failed to open incoming BIO")

            K_BIO_set_close(incomingBio, BIO_CLOSE.convert())

            outgoingBio = BIO_new(BIO_s_mem()) ?: error("Failed to open outgoing BIO")
            K_BIO_set_close(outgoingBio, BIO_CLOSE.convert())
        } catch (e: Throwable) {
            close()
            throw e
        }

        // configure the new SSL object now that we've allocated it
        // 1) openssl will use our in-memory BIOs
        SSL_set_bio(ssl, incomingBio, outgoingBio)
        // 2) set SSL_MODE_ENABLE_PARTIAL_WRITE. this makes SSL_write behave more like we expect.
        //    This imitates the behaviour of write().
        K_SSL_set_mode(ssl, SSL_MODE_ENABLE_PARTIAL_WRITE.convert())

        // 3) tell openssl what side socket we are, and configure appropriately
        if (context.config is TlsClientConfig) {
            SSL_set_connect_state(ssl)
            // 3a) Set hostname for verification.
            SSL_set_hostflags(ssl, X509_CHECK_FLAG_NO_PARTIAL_WILDCARDS)
            tlsError { SSL_set1_host(ssl, hostname) }

            tlsError { K_SSL_set_tlsext_host_name(ssl, hostname) }
        } else if (context.config is TlsServerConfig) {
            SSL_set_accept_state(ssl)
        }
    }

    override fun close() {
        if (!isOpen.compareAndSet(expected = true, new = false)) return
        // Also frees the BIO!
        if (::ssl.isInitialized) SSL_free(ssl)
        // this won't actually free the certificate, but it sets the boolean to prevent it from
        // trying to do operations on the closed handle.
        _peerCertificate?.close()
    }

    /**
     * Returns the number of bytes inside the incoming buffer, that have yet to be consumed by an
     * SSL_read call.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public actual fun incomingPending(): Long {
        if (!isOpen) throw ClosedException("this object is closed")

        return BIO_ctrl_pending(incomingBio).toLong()
    }

    /**
     * Returns the number of bytes inside the outgoing buffer, that have yet to be sent onto the
     * socket.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public actual fun outgoingPending(): Long {
        if (!isOpen) throw ClosedException("this object is closed")

        return BIO_ctrl_pending(outgoingBio).toLong()
    }

    /**
     * Writes incoming encrypted data into this [TlsObject], which will be decrypted for a
     * subsequent [read] call. This method should never fail.
     */
    public actual fun incoming(buf: ByteArray, count: Int, offset: Int) {
        if (!isOpen) throw ClosedException("this object is closed")
        require(count + offset <= buf.size) { "count + offset > data.size" }

        buf.usePinned {
            BIO_write(incomingBio, it.addressOf(offset), count)
        }
    }

    /**
     * Reads out decrypted data from the OpenSSL buffers inside this [TlsObject]. This will read
     * as many available bytes as possible.
     */
    public actual fun read(
        buf: ByteArray, count: Int, offset: Int,
    ): BlockingResult {
        if (!isOpen) throw ClosedException("this object is closed")
        require(count + offset <= buf.size) { "count + offset > buf.size" }

        val res = buf.usePinned {
            SSL_read(ssl, it.addressOf(offset), count)
        }

        return if (res < 0) {
            when (SSL_get_error(ssl, res)) {
                SSL_ERROR_WANT_READ -> BlockingResult.WOULD_BLOCK
                SSL_ERROR_WANT_WRITE -> BlockingResult.WOULD_BLOCK_2
                else -> tlsError()
            }
        } else BlockingResult(res.toLong())
    }

    /**
     * Writes in decrypted data into this [TlsObject], which will be encrypted for a subsequent
     * [outgoing] call.
     */
    public actual fun write(
        buf: ByteArray, count: Int, offset: Int,
    ): BlockingResult {
        if (!isOpen) throw ClosedException("this object is closed")
        require(count + offset <= buf.size) { "count + offset > buf.size" }

        val res = buf.usePinned {
            SSL_write(ssl, it.addressOf(offset), count)
        }

        return if (res < 0) {
            when (SSL_get_error(ssl, res)) {
                SSL_ERROR_WANT_READ -> BlockingResult.WOULD_BLOCK
                SSL_ERROR_WANT_WRITE -> BlockingResult.WOULD_BLOCK_2
                else -> tlsError()
            }
        } else BlockingResult(res.toLong())
    }

    /**
     * Reads out encrypted data from the OpenSSL buffers inside this [TlsObject]. This method should
     * never fail.
     */
    public actual fun outgoing(buf: ByteArray, count: Int, offset: Int): Int {
        if (!isOpen) throw ClosedException("this object is closed")
        require(count + offset <= buf.size) { "count + offset > data.size" }

        return buf.usePinned {
            BIO_read(outgoingBio, it.addressOf(offset), count)
        }
    }

    private val donePostHandshake = AtomicBoolean(false)

    /**
     * Performs some post-handshake operations.
     */
    private fun postHandshake() {
        if (donePostHandshake.compareAndSet(expected = true, new = true)) return
        _peerCertificate = X509Certificate.fromSSL(ssl)
    }

    /**
     * Performs part or all of the TLS handshake.
     *
     * This method should be called repeatedly, taking frames out from outgoing(), and passing
     * frames from the network into incoming(), until this method returns ``true`` to signify that
     * the handshake is complete.
     */
    public actual fun handshake(): Boolean {
        if (!isOpen) throw ClosedException("this object is closed")

        if (SSL_is_init_finished(ssl) == 1) {
            postHandshake()
            return true
        }

        val res = SSL_do_handshake(ssl)
        return when {
            // 1 is handshake complete
            res >= 1 -> {
                postHandshake()
                true
            }
            // 0 is handshake failed successfully (this makes sense!)
            res == 0 -> tlsError()
            res <= -1 -> {
                when (SSL_get_error(ssl, res)) {
                    SSL_ERROR_WANT_WRITE, SSL_ERROR_WANT_READ -> false
                    else -> {
                        tlsError()
                    }
                }
            }
            else -> throw Throwable("This never happens, please file a compiler bug!")
        }
    }

    /**
     * The ALPN protocol that was negotiated during this handshake. This will be null if no protocol
     * was negotiated. This is guaranteed to be one of the protocols set in the configuration.
     */
    public actual val alpnProtocol: String?
        get() = TODO("Not yet implemented")

    /**
     * The TLS version that was negotiated during this handshake.
     */
    public actual val version: TlsVersion
        get() {
            if (!isOpen) throw ClosedException("this object is closed")

            if (SSL_is_init_finished(ssl) != 1) error("Handshake not completed yet")
            return when (val version = SSL_version(ssl)) {
                TLS1_2_VERSION -> TlsVersion.TLS_V12
                TLS1_3_VERSION -> TlsVersion.TLS_V13
                else -> error("Unknown version from SSL_version: $version")
            }
        }

    /**
     * The peer certificate that was sent for this connection. Will be null if this is a server-side
     * context but the client has not sent a certificate.
     *
     * This method is safe because the underlying handle to the certificate is owned by the TLS
     * object, not the certificate object, and will be closed when the TLS object closes.
     */
    public actual val peerCertificate: X509Certificate?
        get() {
            if (!isOpen) throw ClosedException("this object is closed")
            return _peerCertificate
        }
}
