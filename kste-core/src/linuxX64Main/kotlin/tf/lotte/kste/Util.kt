/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste

import kotlinx.cinterop.*

/**
 * Reads out a Kotlin [ByteArray]
 */
public fun CArrayPointer<ByteVar>.readZeroTerminated(): ByteArray {
    var length = 0
    while (true) {
        if (this[length] != 0.toByte()) length += 1
        else break
    }

    return readBytes(length)
}

/**
 * Reads out a Kotlin [ByteArray] from a [CArrayPointer], with maximum size [maxSize] to avoid
 * buffer overflows.
 */
public fun CArrayPointer<ByteVar>.readZeroTerminated(maxSize: Int): ByteArray {
    var length = 0
    while (true) {
        if (length > maxSize) error("Buffer overflow! $length > $maxSize")
        else if (this[length] != 0.toByte()) length += 1
        else break
    }
    return readBytes(length)
}
