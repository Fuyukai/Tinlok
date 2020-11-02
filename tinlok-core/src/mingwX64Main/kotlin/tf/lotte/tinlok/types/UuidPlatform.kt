/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.types

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.windows.*

/**
 * Generates a Version 1 (MAC + Time) UUID.
 */
@OptIn(ExperimentalUnsignedTypes::class)
actual fun uuidGenerateV1(): UByteArray {
    val buf = UByteArray(16)
    buf.usePinned {
        val ptr = it.addressOf(0).reinterpret<UUID>()
        val res = UuidCreateSequential(ptr)
        if (res == RPC_S_UUID_NO_ADDRESS) {
            throw UnsupportedOperationException(
                "Cannot get Ethernet or token-ring hardware address for this computer."
            )
        }
    }

    return buf
}

actual fun uuidGenerateV4(): UByteArray {
    val buf = UByteArray(16)
    buf.usePinned {
        val ptr = it.addressOf(0).reinterpret<UUID>()
        val res = UuidCreate(ptr)
        if (res != RPC_S_OK) {
            error("UuidCreate returned $res (not RPC_S_OK)")
        }
    }

    return buf
}
