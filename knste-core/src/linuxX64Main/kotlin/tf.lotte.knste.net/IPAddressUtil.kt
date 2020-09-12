/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

import tf.lotte.knste.interop.ipv6.*
import kotlinx.cinterop.*
import platform.posix.INET6_ADDRSTRLEN
import platform.posix.htons
import platform.posix.ntohs
import tf.lotte.knste.util.Unsafe

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

    // copy in address from shorts to bytes and in network order
    val ba = ByteArray(32)
    for (x in 0..8) {
        val item = htons(addr.address.components[x])
        // fix byte order
        val upper = ((item.toUInt() shr 8) and 0xFFu).toByte()
        val lower = (item.toUInt() and 0xFFu).toByte()
        ba[x * 2] = upper
        ba[(x * 2) + 1] = lower
    }

    return ba
}

@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
internal actual fun IPv6toString(contents: ByteArray): String = memScoped {
    val addr = alloc<ipv6_address_full_t>()
    // convert to short array, overwrite addr
    for (idx in 0..8) {
        val upper = (contents[idx * 2].toUInt()) shl 8
        val lower = (contents[(idx * 2) + 1].toUInt())
        val combined = ntohs((upper or lower).toUShort())
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
