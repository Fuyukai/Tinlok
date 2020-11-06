/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.cc.Unsafe
import tf.lotte.cc.net.StandardAddressFamilies
import tf.lotte.cc.net.StandardIPProtocols
import tf.lotte.cc.net.StandardSocketTypes
import tf.lotte.cc.net.tcp.TcpConnectionInfo
import tf.lotte.cc.net.tcp.TcpSocketAddress
import tf.lotte.cc.net.udp.UdpConnectionInfo
import tf.lotte.cc.net.udp.UdpSocketAddress
import tf.lotte.tinlok.net.dns.AddressResolver
import tf.lotte.tinlok.net.dns.GlobalResolver

/**
 * Resolves a [host] and [port] combination into a [TcpSocketAddress].
 *
 * @param resolver: The [AddressResolver] to resolve the address with.
 */
@OptIn(Unsafe::class)
public fun TcpSocketAddress.Companion.resolve(
    host: String, port: Int, resolver: AddressResolver = GlobalResolver,
): TcpSocketAddress {
    val connections = resolver.getaddrinfo(
        host = host, service = port,
        family = StandardAddressFamilies.AF_UNSPEC,
        type = StandardSocketTypes.SOCK_STREAM,
        protocol = StandardIPProtocols.IPPROTO_TCP
    ).filterIsInstance<TcpConnectionInfo>()
    return TcpSocketAddress(connections.toSet(), host)
}

/**
 * Resolves a [host] and [port] combination into a [UdpSocketAddress].
 *
 * @param resolver: The [AddressResolver] to resolve the address with.
 */
@OptIn(Unsafe::class)
public fun UdpSocketAddress.Companion.resolve(
    host: String, port: Int, resolver: AddressResolver = GlobalResolver,
): UdpSocketAddress {
    val connections = resolver.getaddrinfo(
        host = host, service = port,
        family = StandardAddressFamilies.AF_UNSPEC,
        type = StandardSocketTypes.SOCK_DGRAM,
        protocol = StandardIPProtocols.IPPROTO_UDP
    ).filterIsInstance<UdpConnectionInfo>()
    return UdpSocketAddress(connections.toSet(), host)
}
