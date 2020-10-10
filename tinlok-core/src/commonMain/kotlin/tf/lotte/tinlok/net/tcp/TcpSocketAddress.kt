/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.tinlok.net.AddressFamily
import tf.lotte.tinlok.net.IPProtocol
import tf.lotte.tinlok.net.SocketType
import tf.lotte.tinlok.net.dns.AddressResolver
import tf.lotte.tinlok.net.dns.GlobalResolver
import tf.lotte.tinlok.net.socket.SocketAddress
import tf.lotte.tinlok.util.Unsafe

/**
 * A socket address for TCP sockets.
 */
public class TcpSocketAddress private constructor(
    private val connections: Set<TcpConnectionInfo>,
) : SocketAddress<TcpConnectionInfo>(), Set<TcpConnectionInfo> by connections {
    public companion object {
        /**
         * Resolves a [host] and [port] combination into a [TcpSocketAddress].
         *
         * @param resolver: The [AddressResolver] to resolve the address with.
         */
        @OptIn(Unsafe::class)
        public fun resolve(
            host: String, port: Int, resolver: AddressResolver = GlobalResolver,
        ): TcpSocketAddress {
            val connections = resolver.getaddrinfo(
                host = host, service = port, family = AddressFamily.AF_UNSPEC,
                type = SocketType.SOCK_STREAM, protocol = IPProtocol.IPPROTO_TCP
            ).filterIsInstance<TcpConnectionInfo>()
            return TcpSocketAddress(connections.toSet())
        }

        /**
         * Creates a new [TcpSocketAddress] from a singular [TcpConnectionInfo].
         */
        public fun of(info: TcpConnectionInfo): TcpSocketAddress {
            return TcpSocketAddress(setOf(info))
        }
    }


    override val protocol: IPProtocol get() = IPProtocol.IPPROTO_TCP

    override fun hashCode(): Int {
        return connections.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is TcpSocketAddress) return false
        if (!connections.containsAll(other.connections)) return false

        return true
    }

    override fun toString(): String {
        return "TcpSocketAddress${connections}"
    }

}
