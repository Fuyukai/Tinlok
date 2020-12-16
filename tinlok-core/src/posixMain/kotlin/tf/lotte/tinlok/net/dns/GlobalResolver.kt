/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.dns

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.udp.UdpConnectionInfo
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.system.toIpPort

/**
 * Cross-platform implementation of the address resolver.
 */
public actual object GlobalResolver : AddressResolver {
    @Unsafe
    override fun getaddrinfo(
        host: String?, service: Int, family: AddressFamily, type: SocketType, protocol: IPProtocol,
        flags: Int,
    ): List<ConnectionInfo> {
        val result = Syscall.getaddrinfo(
            host, service.toString(),
            family.number, type.number, protocol.number,
            flags
        )

        val addresses = ArrayList<ConnectionInfo>(result.size)
        for (info in result) {
            // lookup the values in our enum
            val type = SocketType.values()
                .find { it.number == info.type } ?: continue

            // addresses with a nullptr IP are skipped because ???
            val (ip, port) = info.toIpPort() ?: continue

            val finalAddr = when (type) {
                SocketType.SOCK_STREAM -> {
                    TcpConnectionInfo(ip, port)
                }
                SocketType.SOCK_DGRAM -> {
                    UdpConnectionInfo(ip, port)
                }
                else -> continue  // raw sockets
            }
            addresses.add(finalAddr)
        }

        return addresses
    }
}
