/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.net.socket.PlatformSockets
import tf.lotte.tinlok.net.tcp.TcpClientSocket
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.util.Unsafe

/**
 * A client socket that is encrypted using TLS.
 */
public interface TlsClientSocket : TcpClientSocket {
    public companion object {
        /**
         * Creates a new connected [TlsClientSocket] using the specified [address] and [timeout].
         *
         * This method is unsafe as it can leak file descriptors and memory related to TLS
         * structures.
         */
        @Unsafe
        public fun unsafeOpen(
            address: TcpSocketAddress, config: TlsConfig, timeout: Int
        ): TlsClientSocket {
            return PlatformSockets.newTlsSynchronousSocket(address, config, timeout)
        }
    }

    /** The configuration this socket was created with. */
    public val config: TlsConfig
}
