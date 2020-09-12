/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

import tf.lotte.knste.net.dns.AddressResolver
import tf.lotte.knste.net.dns.GlobalResolver
import tf.lotte.knste.util.Unsafe

/**
 * A socket address for TCP sockets.
 */
public class TcpSocketAddress(
    ip: IPAddress,
    /** The port of this socket address. */
    public val port: Int
) : SocketAddress(ip) {
    public companion object {
        /**
         * Resolves a [host] and [port] combination into a [TcpSocketAddress].
         *
         * @param resolver: The [AddressResolver] to resolve the address with.
         * @param preferIpv6: If IPv6 should be preferred to create the connection. (Default true)
         */
        @OptIn(Unsafe::class)
        public fun resolve(
            host: String, port: Int, resolver: AddressResolver = GlobalResolver,
            preferIpv6: Boolean = true
        ): TcpSocketAddress {
            val addrInfo = resolver.getaddrinfo(
                host, service = port,
                family = AddressFamily.AF_UNSPEC,
                type = SocketKind.SOCK_STREAM,
                protocol = IPProtocol.IPPROTO_TCP
            )
            return if (preferIpv6) {
                addrInfo.find { it.family == AddressFamily.AF_INET } as TcpSocketAddress
            } else {
                addrInfo.firstOrNull() as? TcpSocketAddress
            } ?: TODO("handle appropriate error")
        }
    }

    override val family: AddressFamily get() = ipAddress.family
    override val kind: SocketKind get() = SocketKind.SOCK_STREAM
    override val protocol: IPProtocol get() = IPProtocol.IPPROTO_TCP


    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + port
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is TcpSocketAddress) return false
        if (!super.equals(other)) return false
        if (port != other.port) return false

        return true
    }


}
