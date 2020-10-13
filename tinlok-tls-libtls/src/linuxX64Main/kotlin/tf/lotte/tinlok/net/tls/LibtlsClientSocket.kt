/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import tf.lotte.tinlok.exc.ClosedException
import tf.lotte.tinlok.interop.libtls.*
import tf.lotte.tinlok.net.socket.PlatformSockets
import tf.lotte.tinlok.net.socket.StandardSocketOption
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.util.Unsafe
import kotlin.native.concurrent.AtomicInt

/**
 * Implements TLS using libtls.
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal actual class LibtlsClientSocket
constructor(
    private val fd: Int,
    private val _hostname: String,  // underscored as it's diff to the potential server name.
    override val config: TlsConfig,
    override val remoteAddress: TcpConnectionInfo
) : TlsClientSocket {
    // hack for lack of AtomicBoolean
    private val _closed = AtomicInt(0)
    private var tlsContext = tls_client()
    private var tlsConfig = tls_config_new()

    /** Booleanifies the _closed value */
    val closed: Boolean
        get() = _closed.value == 1

    /**
     * Frees all of the TLS structures, and closes the file descriptor.
     */
    @OptIn(Unsafe::class)
    private fun free() {
        // avoid double-frees by guarding with the "atomic boolean"
        if (_closed.value != 0) return
        _closed.addAndGet(1)

        try {
            Syscall.close(fd)
        } finally {
            // if libtls let me allocatee these normally...
            // i could just use an arena
            if (tlsConfig != null) {
                tls_config_free(tlsConfig)

            }
            if (tlsContext != null) {
                tls_free(tlsContext)
            }
        }

    }

    /** Raises if a configuration error happens */
    private inline fun cerr(block: () -> Int): Int {
        val res = block()
        if (res == -1) {
            val err = tls_config_error(tlsConfig)?.toKString() ?: "unknown error"
            free()
            throw TlsException("config failure: $err")
        }
        return res
    }

    /** Raises if a context error happens */
    private inline fun xerr(block: () -> Int): Int {
        val res = block()
        if (res == -1) {
            val err = tls_error(tlsContext)?.toKString() ?: "unknown error"
            free()
            throw TlsException(err)
        }
        return res
    }

    init {
        if (tlsConfig == null) {
            free()
            error("Unable to allocate TLS config")
        }
        if (tlsContext == null) {
            free()
            error("Unable to allocate TLS context")
        }

        if (config.useTlsv12) {
            val bits = TLS_PROTOCOL_TLSv1_3.or(TLS_PROTOCOL_TLSv1_2)
            cerr { tls_config_set_protocols(tlsConfig, bits.toUInt()) }
        } else {
            cerr { tls_config_set_protocols(tlsConfig, TLS_PROTOCOL_TLSv1_3) }
        }

        if (config.alpnProtocols.isNotEmpty()) {
            val alpn = config.alpnProtocols.joinToString(",")
            cerr { tls_config_set_alpn(tlsConfig, alpn) }
        }

        xerr { tls_configure(tlsContext, tlsConfig) }
        xerr { tls_connect_socket(tlsContext, fd, _hostname) }
    }

    @OptIn(Unsafe::class)
    override fun close() {
        // i don't actually care what tls_close returns
        // given we're about to destroy the connection
        if (!closed)  {
            tls_close(tlsContext)
            free()
        }
    }

    @OptIn(Unsafe::class)
    override fun <T> getSocketOption(option: StandardSocketOption<T>): T {
        if (closed) throw ClosedException("This socket is closed")
        return Syscall.getsockopt(fd, option)
    }

    @OptIn(Unsafe::class)
    override fun <T> setSocketOption(option: StandardSocketOption<T>, value: T) {
        if (closed) throw ClosedException("This socket is closed")
        return Syscall.setsockopt(fd, option, value)
    }

    // TLS I/O
    // i love writing two retry loops
    override fun readUpTo(bytes: Long): ByteString? {
        if (closed) throw ClosedException("This socket is closed")

        val buf = ByteArray(bytes.toInt())
        buf.usePinned {
            while (true) {
                val read = xerr {
                    tls_read(tlsContext, it.addressOf(0), buf.size.toULong()).toInt()
                }
                if (read == TLS_WANT_POLLIN || read == TLS_WANT_POLLOUT) continue

                return if (read == bytes.toInt()) ByteString.fromByteArray(buf)
                else {
                    println("forcing copy")
                    val copy = buf.copyOfRange(0, read)
                    ByteString.fromByteArray(copy)
                }
            }
        }

        throw Throwable("unreachable code; severe bug; please report!")
    }

    override fun writeAll(bs: ByteString) {
        if (closed) throw ClosedException("This socket is closed")

        val buf = bs.unwrapCopy()
        buf.usePinned {
            var ptr = 0
            var length = buf.size

            while (true) {
                val written = xerr {
                    tls_write(tlsContext, it.addressOf(ptr), length.toULong()).toInt()
                }
                if (written == TLS_WANT_POLLIN || written == TLS_WANT_POLLOUT) continue

                ptr += written
                length -= written
                if (length <= 0) {
                    break
                }
            }
        }
    }

    override fun sendEof() {
        if (closed) throw ClosedException("This socket is closed")
        // lie blatantly and close both ends
        close()
    }

}

/**
 * Opens a new connected TLS synchronous socket.
 */
@Unsafe
public actual fun PlatformSockets.newTlsSynchronousSocket(
    address: TcpSocketAddress, config: TlsConfig, timeout: Int
): TlsClientSocket {
    requireNotNull(address.hostname) { "Address hostname must not be null" }
    val (fd, info) = tryTcpConnect(address, timeout)
    return LibtlsClientSocket(fd, address.hostname!!, config, info)
}
