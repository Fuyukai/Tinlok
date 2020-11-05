/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.arrayMemberAt
import kotlinx.cinterop.reinterpret
import platform.posix.*
import tf.lotte.cc.Unsafe
import tf.lotte.cc.util.toByteArray
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.system.readBytesFast

/**
 * Decodes a C socket address into a Pair<IPAddress, Int>, or null if this address family is
 * unsupported.
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
public fun sockaddr.toKotlin(family: AddressFamily): Pair<IPAddress, Int>? {
    return when (family) {
        AddressFamily.AF_INET -> {
            val real = reinterpret<sockaddr_in>()
            val ipBytes = real.sin_addr.s_addr.toByteArray()
            val ip = IPv4Address(ipBytes)
            return ip to ntohs(real.sin_port).toInt()
        }
        AddressFamily.AF_INET6 -> {
            val real = reinterpret<sockaddr_in6>()
            // XX: Kotlin in6_addr has no fields!
            val addrPtr = real.sin6_addr.arrayMemberAt<ByteVar>(0L)
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
    val familyInt = ss_family.toInt()
    val family = AddressFamily.values().find { it.number == familyInt } ?: return null
    return reinterpret<sockaddr>().toKotlin(family)
}