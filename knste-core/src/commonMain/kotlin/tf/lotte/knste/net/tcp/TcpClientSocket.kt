/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.io.HalfCloseableStream
import tf.lotte.knste.net.socket.ClientSocket
import tf.lotte.knste.net.socket.PlatformSockets
import tf.lotte.knste.net.socket.Socket
import tf.lotte.knste.util.Unsafe

/**
 * Interface defining a TCP synchronous socket.
 *
 * This is simply a [HalfCloseableStream] combined with a [Socket].
 */
public interface TcpClientSocket : ClientSocket<TcpSocketAddress> {
    public companion object {
        /**
         * Creates a new [TcpClientSocket] from a platform socket.
         *
         * This method is unsafe as it can leak file descriptors.
         */
        @Unsafe
        public fun unsafeOpen(): TcpClientSocket {
            return PlatformSockets.newTcpSynchronousSocket()
        }
    }
}
