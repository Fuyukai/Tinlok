/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import platform.posix.sockaddr
import platform.posix.sockaddr_in
import platform.posix.sockaddr_storage
import platform.windows.ntohs
import platform.windows.sockaddr_in6
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.*

/**
 * Decodes a C socket address into a Pair<IPAddress, Int>, or null if this address family is
 * unsupported.
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
public fun sockaddr.toKotlin(): Pair<IPAddress, Int>? {
    return when (sa_family.toInt()) {
        AddressFamily.AF_INET.number -> {
            val real = reinterpret<sockaddr_in>()

            val ipBytes = real.sin_addr.S_un.S_addr.toByteArray()
            val ip = IPv4Address(ipBytes)
            return ip to ntohs(real.sin_port).toInt()
        }
        AddressFamily.AF_INET6.number -> {
            val real = reinterpret<sockaddr_in6>()
            val addrPtr = real.sin6_addr.u.Byte
            val addr = addrPtr.readBytesFast(16)
            val ip = IPv6Address(addr)
            val port = ntohs(real.sin6_port).toInt()
            return ip to port
        }
        else -> null  // unknown address family
    }
}

/**
 * Decodes a C sockaddr_storage into a Pair<IPAddress, Int>.
 */
@Unsafe
public fun sockaddr_storage.toKotlin(): Pair<IPAddress, Int>? {
    return reinterpret<sockaddr>().toKotlin()
}
