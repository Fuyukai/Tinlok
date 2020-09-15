/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.udp

import tf.lotte.tinlok.net.IPProtocol
import tf.lotte.tinlok.net.socket.SocketAddress

/**
 * A socket address for UDP sockets.
 */
public class UdpSocketAddress private constructor(
    private val connections: Set<UdpConnectionInfo>,
) : SocketAddress<UdpConnectionInfo>(), Set<UdpConnectionInfo> by connections {
    override val protocol: IPProtocol = IPProtocol.IPPROTO_UDP

    override fun hashCode(): Int {
        return connections.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is UdpSocketAddress) return false
        if (!connections.containsAll(other.connections)) return false

        return true
    }
}
