/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.cc.Unsafe
import tf.lotte.cc.net.*
import tf.lotte.tinlok.net.dns.AddressResolver
import tf.lotte.tinlok.net.dns.GlobalResolver
import tf.lotte.tinlok.net.socket.SocketAddress

/**
 * A socket address for TCP sockets.
 */
public class TcpSocketAddress private constructor(
    override val hostname: String?,
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
                host = host, service = port,
                family = StandardAddressFamilies.AF_UNSPEC,
                type = StandardSocketTypes.SOCK_STREAM,
                protocol = StandardIPProtocols.IPPROTO_TCP
            ).filterIsInstance<TcpConnectionInfo>()
            return TcpSocketAddress(host, connections.toSet())
        }

        /**
         * Creates a new [TcpSocketAddress] from a singular [TcpConnectionInfo].
         */
        public fun of(info: TcpConnectionInfo, host: String? = null): TcpSocketAddress {
            return TcpSocketAddress(host ?: info.ip.toString(), setOf(info))
        }
    }

    override val protocol: IPProtocol get() = StandardIPProtocols.IPPROTO_TCP

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
