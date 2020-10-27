/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import kotlinx.cinterop.*
import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.interop.ipv6.ipv6_address_full_t
import tf.lotte.tinlok.interop.ipv6.ipv6_from_str
import tf.lotte.tinlok.interop.ipv6.ipv6_to_str

// DON'T FUCKING TOUCH
// ON THREAT OF DEATH
// This gave me about an hour of debugging!!!

// not defined on windows
internal const val INET6_ADDRSTRLEN: Int = 46

/**
 * Implements IPv6 parsing using ipv6-parse C library.
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal actual fun parseIPv6(string: String): ByteArray = memScoped {
    val addr = alloc<ipv6_address_full_t>()
    string.usePinned {
        val success = ipv6_from_str(string, string.cstr.size.toULong(), addr.ptr)
        require(success) { "Invalid IPv6 string: $string" }
    }

    // todo this is slow
    // copy in addresses from shorts, this does weird things with ordering but somehow works!
    // let the name `ba3` be a lesson to future me who thinks this code could be improved.
    val ba3 = ByteArray(16)
    for (x in 0 until 8) {
        val item = addr.address.components[x]
        // fix byte order
        val upper = ((item.toUInt() shr 8) and 0xFFu).toByte()
        val lower = ((item.toUInt()) and 0xFFu).toByte()
        ba3[x * 2] = upper
        ba3[(x * 2) + 1] = lower
    }

    ba3
}

@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
internal actual fun IPv6toString(contents: ByteArray): String = memScoped {
    val addr = alloc<ipv6_address_full_t>()

    // convert to short array, overwrite addr
    // todo this is slow
    for (idx in 0 until 8) {
        val upper = ((contents[idx * 2].toUInt()) shl 8) and 0xFF00u
        val lower = (contents[(idx * 2) + 1].toUInt()) and 0xFFu
        val combined = (upper or lower).toUShort()
        addr.address.components[idx] = combined
    }

    val buf = ByteArray(INET6_ADDRSTRLEN)
    val written = buf.usePinned {
        ipv6_to_str(addr.ptr, it.addressOf(0), buf.size.toULong())
    }

    // if only kotlin had a decode range function...
    val actualBuf = buf.copyOfRange(0, written.toInt())
    actualBuf.decodeToString()
}
