/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.udp

import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.net.AddressFamily
import tf.lotte.tinlok.net.IPProtocol
import tf.lotte.tinlok.net.SocketType
import tf.lotte.tinlok.net.dns.AddressResolver
import tf.lotte.tinlok.net.dns.GlobalResolver
import tf.lotte.tinlok.net.socket.SocketAddress

/**
 * A socket address for UDP sockets.
 */
public class UdpSocketAddress private constructor(
    override val hostname: String?,
    private val connections: Set<UdpConnectionInfo>,
) : SocketAddress<UdpConnectionInfo>(), Set<UdpConnectionInfo> by connections {
    public companion object {
        /**
         * Resolves a [host] and [port] combination into a [UdpSocketAddress].
         *
         * @param resolver: The [AddressResolver] to resolve the address with.
         */
        @OptIn(Unsafe::class)
        public fun resolve(
            host: String, port: Int, resolver: AddressResolver = GlobalResolver,
        ): UdpSocketAddress {
            val connections = resolver.getaddrinfo(
                host = host, service = port, family = AddressFamily.AF_UNSPEC,
                type = SocketType.SOCK_DGRAM, protocol = IPProtocol.IPPROTO_UDP
            ).filterIsInstance<UdpConnectionInfo>()
            return UdpSocketAddress(host, connections.toSet())
        }
    }

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
