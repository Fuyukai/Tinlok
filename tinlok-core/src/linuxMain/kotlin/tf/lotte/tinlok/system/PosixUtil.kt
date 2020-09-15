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
import tf.lotte.tinlok.util.Unsafe

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

    val buf = ByteArray(length)
    Syscall.__fast_ptr_to_bytearray(this, buf, length)
    return buf
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
    val buf = ByteArray(length)
    Syscall.__fast_ptr_to_bytearray(this, buf, length)
    return buf
}

/**
 * Reads bytes from a [COpaquePointer] using __fast_ptr_to_bytearray instead of the naiive Kotlin
 * byte-by-byte copy.
 */
@Unsafe
public fun COpaquePointer.readBytesFast(count: Int): ByteArray {
    val buf = ByteArray(count)
    Syscall.__fast_ptr_to_bytearray(this, buf, count)
    return buf
}


/**
 * Overwrites the memory pointed to with a [ByteArray].
 */
@Unsafe
public fun CArrayPointer<ByteVar>.unsafeClobber(other: ByteArray) {
    for (idx in other.indices) {
        this[idx] = other[idx]
    }
}

/**
 * Creates a pointer to a pinned Long.
 */
public fun NativePlacement.ptrTo(value: Pinned<Long>): CPointer<LongVar> {
    val var_ = alloc<LongVar>()
    var_.value = value.get()
    return var_.ptr
}
