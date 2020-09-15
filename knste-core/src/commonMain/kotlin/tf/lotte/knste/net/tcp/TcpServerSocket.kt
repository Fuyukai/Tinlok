/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.net.socket.PlatformSockets
import tf.lotte.knste.net.socket.ServerSocket
import tf.lotte.knste.util.Unsafe

/**
 * Interface defining a TCP synchronous server socket that produces [TcpClientSocket]
 * instances on ``accept()``.
 */
public interface TcpServerSocket :
    ServerSocket<TcpConnectionInfo, TcpSocketAddress, TcpClientSocket> {
    public companion object {
        /**
         * Opens a new unbound [TcpServerSocket] from a platform socket.
         *
         * This method is unsafe as it can leak file descriptors.
         */
        @Unsafe
        public fun unsafeOpen(
            address: TcpConnectionInfo,
        ): TcpServerSocket {
            return PlatformSockets.newTcpSynchronousServerSocket(address)
        }
    }
}
