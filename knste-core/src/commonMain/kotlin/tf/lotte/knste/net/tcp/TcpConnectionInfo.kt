/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.net.*

/**
 * Connection information for TCP sockets.
 */
public class TcpConnectionInfo(
    ip: IPAddress, port: Int,
) : InetConnectionInfo(ip, port) {
    override val family: AddressFamily get() {
        return when (ip) {
            is IPv4Address -> AddressFamily.AF_INET
            is IPv6Address -> AddressFamily.AF_INET6
        }
    }

    override val protocol: IPProtocol get() = IPProtocol.IPPROTO_TCP
    override val type: SocketType get() = SocketType.SOCK_STREAM
}
