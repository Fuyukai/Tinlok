/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.net.tcp

import tf.lotte.cc.net.*

/**
 * Connection information for TCP sockets.
 */
public class TcpConnectionInfo(ip: IPAddress, port: Int) : InetConnectionInfo(ip, port) {
    public companion object {
        /**
         * Creates a new [TcpConnectionInfo] representing the wildcard address, for binding a
         * socket to.
         */
        public fun wildcard(port: Int): TcpConnectionInfo {
            // this is the IPv6 wildcard
            // if you want to use the IPv4 wildcard on a non-dualstack system (what year is it!)
            // you have to make it yourself.
            return TcpConnectionInfo(IPv6Address.of("::"), port)
        }

        /**
         * Creates a new [TcpConnectionInfo] representing the localhost address, for binding a
         * socket to.
         */
        public fun localhost(port: Int): TcpConnectionInfo {
            return TcpConnectionInfo(IPv6Address.of("::1"), port)
        }
    }

    override val family: AddressFamily
        get() {
            return when (ip) {
                is IPv4Address -> StandardAddressFamilies.AF_INET
                is IPv6Address -> StandardAddressFamilies.AF_INET6
            }
        }

    override val protocol: IPProtocol get() = StandardIPProtocols.IPPROTO_TCP
    override val type: SocketType get() = StandardSocketTypes.SOCK_STREAM

    override fun toString(): String {
        return "TcpConnectionInfo($ip, $port)"
    }
}
