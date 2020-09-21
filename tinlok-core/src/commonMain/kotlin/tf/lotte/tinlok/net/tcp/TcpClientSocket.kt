/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.tinlok.io.HalfCloseableStream
import tf.lotte.tinlok.net.socket.ClientSocket
import tf.lotte.tinlok.net.socket.PlatformSockets
import tf.lotte.tinlok.net.socket.Socket
import tf.lotte.tinlok.util.Unsafe

/**
 * Interface defining a TCP synchronous socket.
 *
 * This is simply a [HalfCloseableStream] combined with a [Socket].
 */
public interface TcpClientSocket : TcpSocket, ClientSocket<TcpConnectionInfo> {
    public companion object {
        /**
         * Creates a new [TcpClientSocket] from a platform socket.
         *
         * This method is unsafe as it can leak file descriptors.
         */
        @Unsafe
        public fun unsafeOpen(address: TcpSocketAddress): TcpClientSocket {
            return PlatformSockets.newTcpSynchronousSocket(address)
        }
    }
}
