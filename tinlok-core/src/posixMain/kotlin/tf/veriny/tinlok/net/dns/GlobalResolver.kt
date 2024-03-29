/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.dns

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.net.AddressFamily
import tf.veriny.tinlok.net.ConnectionInfo
import tf.veriny.tinlok.net.IPProtocol
import tf.veriny.tinlok.net.SocketType
import tf.veriny.tinlok.net.tcp.TcpConnectionInfo
import tf.veriny.tinlok.net.udp.UdpConnectionInfo
import tf.veriny.tinlok.system.Syscall
import tf.veriny.tinlok.system.toIpPort

/**
 * Cross-platform implementation of the address resolver.
 */
public actual object GlobalResolver : AddressResolver {
    @Unsafe
    override fun getaddrinfo(
        host: String?,
        service: Int,
        family: AddressFamily,
        type: SocketType,
        protocol: IPProtocol,
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
            val socketType = SocketType.values()
                .find { it.number == info.type } ?: continue

            // addresses with a nullptr IP are skipped because ???
            val (ip, port) = info.toIpPort() ?: continue

            val finalAddr = when (socketType) {
                SocketType.SOCK_STREAM -> {
                    TcpConnectionInfo(ip, port)
                }
                SocketType.SOCK_DGRAM -> {
                    UdpConnectionInfo(ip, port)
                }
                else -> continue // raw sockets
            }
            addresses.add(finalAddr)
        }

        return addresses
    }
}
