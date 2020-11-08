/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.dns

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import platform.posix.addrinfo
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.udp.UdpConnectionInfo
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.toKotlin

@ExperimentalUnsignedTypes
public actual object GlobalResolver : AddressResolver {
    @Unsafe
    private fun unrollAddrInfo(first: addrinfo): List<ConnectionInfo> = try {
        // unroll addrinfos into a list so we can do safe continues
        val cAddresses = run {
            var lastInfo = first
            val infos = mutableListOf<addrinfo>()
            while (true) {
                infos.add(lastInfo)
                if (lastInfo.ai_next == null) break
                lastInfo = lastInfo.ai_next!!.pointed
            }
            infos
        }

        // now we create our own SocketAddress instances
        val addresses = ArrayList<ConnectionInfo>(cAddresses.size)
        for (info in cAddresses) {
            // lookup the values in our enum
            val family = StandardAddressFamilies.values()
                .find { it.number == info.ai_family } ?: continue
            val type = StandardSocketTypes.values()
                .find { it.number == info.ai_socktype } ?: continue
            val protocol = StandardIPProtocols.values()
                .find { it.number == info.ai_protocol } ?: continue

            // addresses with a nullptr IP are skipped because ???
            val sockaddr = info.ai_addr?.pointed ?: continue
            val (ip, port) = sockaddr.toKotlin(family) ?: continue

            val finalAddr = when (type) {
                StandardSocketTypes.SOCK_STREAM -> {
                    TcpConnectionInfo(ip, port)
                }
                StandardSocketTypes.SOCK_DGRAM -> {
                    UdpConnectionInfo(ip, port)
                }
                else -> continue  // raw sockets
            }
            addresses.add(finalAddr)
        }

        addresses
    } finally {
        Syscall.freeaddrinfo(first.ptr)
    }

    @Suppress("NAME_SHADOWING")
    @Unsafe
    override fun getaddrinfo(
        host: String?, service: Int,
        family: AddressFamily,
        type: SocketType, protocol: IPProtocol,
        flags: Int,
    ): List<ConnectionInfo> {
        val strService = if (service == 0) null else service.toString()
        return memScoped {
            val firstAddr = Syscall.getaddrinfo(
                this, host, strService,
                family.number, type.number,
                protocol = protocol.number,
                flags = flags
            )

            unrollAddrInfo(firstAddr)
        }
    }
}

