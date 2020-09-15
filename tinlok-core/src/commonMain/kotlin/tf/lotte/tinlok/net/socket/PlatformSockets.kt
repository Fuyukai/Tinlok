/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.net.tcp.TcpClientSocket
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.tcp.TcpServerSocket
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.util.Unsafe

/**
 * Expect object providing the creation of socket instances.
 */
@Unsafe
public expect object PlatformSockets {
    /**
     * Creates a new unconnected [TcpClientSocket].
     */
    @Unsafe
    public fun newTcpSynchronousSocket(address: TcpSocketAddress): TcpClientSocket

    /**
     * Creates a new [TcpServerSocket].
     */
    @Unsafe
    public fun newTcpSynchronousServerSocket(address: TcpConnectionInfo): TcpServerSocket
}
