/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

/**
 * A datagram address for usage with UDP.
 */
public class DatagramSocketAddress(ip: IPAddress, public val port: Int) : InetSocketAddress(ip) {
    override val family: AddressFamily = ip.family
    override val kind: SocketKind = SocketKind.SOCK_DGRAM
    override val protocol: IPProtocol = IPProtocol.IPPROTO_UDP
}
