/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.tls.x509.X509Certificate
import tf.lotte.tinlok.system.BlockingResult
import tf.lotte.tinlok.util.Closeable

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
@Unsafe
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class TlsObject(
    context: TlsContext,
    hostname: String,
) : Closeable {
    /** The [TlsContext] this [TlsObject] uses. */
    public val context: TlsContext

    // == I/O == //
    /**
     * Returns the number of bytes inside the incoming buffer, that have yet to be consumed by an
     * SSL_read call.
     */
    public fun incomingPending(): Long

    /**
     * Returns the number of bytes inside the outgoing buffer, that have yet to be sent onto the
     * socket.
     */
    public fun outgoingPending(): Long

    /**
     * Writes incoming encrypted data into this [TlsObject], which will be decrypted for a
     * subsequent [read] call.
     */
    public fun incoming(buf: ByteArray, count: Int, offset: Int)

    /**
     * Reads out decrypted data from the OpenSSL buffers inside this [TlsObject]. This will read
     * as many available bytes as possible.
     */
    public fun read(buf: ByteArray, count: Int, offset: Int): BlockingResult

    /**
     * Writes in decrypted data into this [TlsObject], which will be encrypted for a subsequent
     * [outgoing] call.
     */
    public fun write(buf: ByteArray, count: Int, offset: Int): BlockingResult

    /**
     * Reads out encrypted data from the OpenSSL buffers inside this [TlsObject], returning the
     * number of bytes written to [buf].
     *
     * This method may need to write more to [buf] than the input size; call this repeatedly until
     * this method returns 0 (EOF).
     */
    public fun outgoing(buf: ByteArray, count: Int, offset: Int): Int

    /**
     * Performs part or all of the TLS handshake.
     *
     * This method should be called repeatedly, taking frames out from outgoing(), and passing
     * frames from the network into incoming(), until this method returns ``true`` to signify that
     * the handshake is complete.
     */
    public fun handshake(): Boolean

    // == Properties == //
    /**
     * The ALPN protocol that was negotiated during this handshake. This will be null if no protocol
     * was negotiated. This is guaranteed to be one of the protocols set in the configuration.
     */
    public val alpnProtocol: String?

    /**
     * The TLS version that was negotiated during this handshake.
     */
    public val version: TlsVersion

    /**
     * The peer certificate that was sent for this connection. Will be null if this is a server-side
     * context but the client has not sent a certificate.
     *
     * This method is safe because the underlying handle to the certificate is owned by the TLS
     * object, not the certificate object, and will be closed when the TLS object closes.
     */
    public val peerCertificate: X509Certificate?

}
