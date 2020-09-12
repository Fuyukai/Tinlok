/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.udp

import tf.lotte.knste.net.*

/**
 * Connection information for UDP sockets.
 */
public class UdpConnectionInfo(ip: IPAddress, port: Int) : InetConnectionInfo(ip, port) {
    override val family: AddressFamily get() = ip.family
    override val protocol: IPProtocol get() = IPProtocol.IPPROTO_UDP
    override val type: SocketType = SocketType.SOCK_DGRAM
}
