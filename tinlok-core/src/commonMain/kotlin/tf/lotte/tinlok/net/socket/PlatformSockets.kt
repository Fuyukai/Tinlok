/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.net.tcp.*

/**
 * Expect object providing the creation of socket instances.
 */
@Unsafe
public expect object PlatformSockets {
    /**
     * Creates a new unconnected [TcpClientSocket].
     */
    @Unsafe
    public fun newTcpSynchronousSocket(address: TcpSocketAddress, timeout: Int): TcpClientSocket

    /**
     * Creates a new [TcpServerSocket].
     */
    @Unsafe
    public fun newTcpSynchronousServerSocket(address: TcpConnectionInfo): TcpServerSocket
}
