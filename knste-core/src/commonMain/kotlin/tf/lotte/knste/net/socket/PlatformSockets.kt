/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.socket

import tf.lotte.knste.net.tcp.TcpClientSocket
import tf.lotte.knste.net.tcp.TcpServerSocket
import tf.lotte.knste.util.Unsafe

/**
 * Expect object providing the creation of socket instances.
 */
@Unsafe
public expect object PlatformSockets {
    /**
     * Creates a new unconnected [TcpClientSocket].
     */
    @Unsafe
    public fun newTcpSynchronousSocket(): TcpClientSocket

    /**
     * Creates a new unbinded [TcpServerSocket].
     */
    @Unsafe
    public fun newTcpSynchronousServerSocket(): TcpServerSocket
}
