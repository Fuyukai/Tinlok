/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package tf.lotte.tinlok.util

import external.libuuid.uuid_generate_random
import external.libuuid.uuid_generate_time_safe
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

/**
 * Generates a version 1 UUID.
 */
@Suppress("ControlFlowWithEmptyBody")
public actual fun uuidGenerateV1(): UByteArray {
    val buf = UByteArray(16)
    buf.usePinned {
        val res = uuid_generate_time_safe(it.addressOf(0))
        if (res != 0) {
            // TODO: A warning on unsafe time-based UUIDs
        }
    }
    return buf
}

/**
 * Generates a version 4 UUID.
 */
public actual fun uuidGenerateV4(): UByteArray {
    val ba = UByteArray(16)
    ba.usePinned {
        uuid_generate_random(it.addressOf(0))
    }
    return ba
}
