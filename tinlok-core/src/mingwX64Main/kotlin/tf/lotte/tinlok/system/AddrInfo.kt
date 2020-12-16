/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import kotlinx.cinterop.*
import platform.posix.sockaddr
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.IPAddress

/**
 * Converts the [addr] inside this [AddrInfo] to an IP/Port combo.
 */
@OptIn(Unsafe::class)
public actual fun AddrInfo.toIpPort(): Pair<IPAddress, Int>? {
    addr.usePinned {
        val ptr = it.addressOf(0).reinterpret<sockaddr>().pointed
        return ptr.toKotlin()
    }
}
