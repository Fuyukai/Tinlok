/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.cc.Unsafe
import tf.lotte.cc.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.socket.AcceptingSeverSocket
import tf.lotte.tinlok.net.socket.PlatformSockets

/**
 * Interface defining a TCP synchronous server socket that produces [TcpClientSocket]
 * instances on ``accept()``.
 */
public interface TcpServerSocket : AcceptingSeverSocket<TcpConnectionInfo, TcpClientSocket> {
    public companion object {
        /**
         * Opens a new unbound [TcpServerSocket] from a platform socket.
         *
         * This method is unsafe as it can leak file descriptors.
         */
        @Unsafe
        public fun unsafeOpen(address: TcpConnectionInfo): TcpServerSocket {
            return PlatformSockets.newTcpSynchronousServerSocket(address)
        }
    }
}
