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
import tf.lotte.tinlok.Unsafe

/**
 * Reads out a Kotlin [ByteArray] from a [CArrayPointer].
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
public fun CArrayPointer<ByteVar>.readZeroTerminated(): ByteArray {
    val length = __strlen(this)
    require(length < UInt.MAX_VALUE) { "Size $length is too big" }

    val buf = ByteArray(length.toInt())
    Syscall.__fast_ptr_to_bytearray(this, buf, buf.size)
    return buf
}

/**
 * Reads out a Kotlin [ByteArray] from a [CArrayPointer], with maximum size [maxSize] to avoid
 * buffer overflows.
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
public fun CArrayPointer<ByteVar>.readZeroTerminated(maxSize: Int): ByteArray {
    val length = __strnlen(this, maxSize.toULong())
    require(length < UInt.MAX_VALUE) { "Size $length is too big" }

    val buf = ByteArray(length.toInt())
    Syscall.__fast_ptr_to_bytearray(this, buf, buf.size)
    return buf
}

/**
 * Reads out a Kotlin [String] from a [CArrayPointer], but faster than the native method.
 */
@Unsafe
public fun CPointer<ByteVar>.toKStringUtf8Fast(): String {
    val ba = readZeroTerminated()
    return ba.decodeToString()
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
