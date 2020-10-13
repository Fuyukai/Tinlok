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
import tf.lotte.tinlok.net.socket.PlatformSockets
import tf.lotte.tinlok.net.socket.Socket
import tf.lotte.tinlok.net.socket.StreamingClientSocket
import tf.lotte.tinlok.util.Unsafe

/**
 * Interface defining a TCP synchronous socket.
 *
 * This is simply a [HalfCloseableStream] combined with a [Socket].
 */
public interface TcpClientSocket : TcpSocket, StreamingClientSocket<TcpConnectionInfo> {
    public companion object {
        /**
         * Creates a new connected [TcpClientSocket] using the specified [address] and [timeout].
         *
         * This method is unsafe as it can leak file descriptors.
         */
        @Unsafe
        public fun unsafeOpen(address: TcpSocketAddress, timeout: Int): TcpClientSocket {
            return PlatformSockets.newTcpSynchronousSocket(address, timeout)
        }
    }
}
