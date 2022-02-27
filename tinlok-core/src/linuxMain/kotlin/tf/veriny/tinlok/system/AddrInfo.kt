/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.system

import kotlinx.cinterop.*
import platform.posix.sockaddr
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.net.IPAddress
import tf.veriny.tinlok.util.toKotlin

/**
 * Converts the [AddrInfo.addr] inside this [AddrInfo] to an IP/Port combo.
 */
@OptIn(Unsafe::class)
public actual fun AddrInfo.toIpPort(): Pair<IPAddress, Int>? {
    addr.usePinned {
        val ptr = it.addressOf(0).reinterpret<sockaddr>().pointed
        return ptr.toKotlin()
    }
}
