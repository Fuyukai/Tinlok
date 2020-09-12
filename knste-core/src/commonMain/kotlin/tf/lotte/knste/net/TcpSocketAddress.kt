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
) : InetSocketAddress(ip) {
    public companion object {
        /**
         * Resolves a [host] and [port] combination into a [TcpSocketAddress].
         *
         * @param resolver: The [AddressResolver] to resolve the address with.
         * @param preferIpv6: If IPv6 should be preferred when resolving (default true).
         */
        @OptIn(Unsafe::class)
        public fun resolve(
            host: String, port: Int, resolver: AddressResolver = GlobalResolver,
        ): TcpSocketAddress {
            TODO()
        }

        /**
         * Creates a new [TcpSocketAddress] for the wildcard address, suitable for binding.
         *
         * @param resolver: The [AddressResolver] to resolve the wildcard address with.
         * @param preferIpv6: If IPv6 should be preferred when resolving (default true).
         */
        @OptIn(Unsafe::class)
        public fun wildcard(
            port: Int,
            resolver: AddressResolver = GlobalResolver,
            preferIpv6: Boolean = true
        ): TcpSocketAddress {
            TODO()
        }
    }

    override val family: AddressFamily get() = ip.family
    override val type: SocketType get() = SocketType.SOCK_STREAM
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
