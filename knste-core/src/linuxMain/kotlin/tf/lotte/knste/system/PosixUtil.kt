/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.system

import kotlinx.cinterop.*
import tf.lotte.knste.util.Unsafe

/**
 * Reads out a Kotlin [ByteArray] from a [CArrayPointer].
 */
@Unsafe
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
@Unsafe
public fun CArrayPointer<ByteVar>.readZeroTerminated(maxSize: Int): ByteArray {
    var length = 0
    while (true) {
        if (length > maxSize) error("Buffer overflow! $length > $maxSize")
        else if (this[length] != 0.toByte()) length += 1
        else break
    }
    return readBytes(length)
}

/**
 * Creates a pointer to a pinned Long.
 */
public fun NativePlacement.ptrTo(value: Pinned<Long>): CPointer<LongVar> {
    val var_ = alloc<LongVar>()
    var_.value = value.get()
    return var_.ptr
}
