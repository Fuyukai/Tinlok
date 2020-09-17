/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import kotlinx.cinterop.COpaquePointer
import tf.lotte.tinlok.util.Unsafe

/**
 * Namespace object for all libc bindings.
 *
 * In the common package, only a small subset of functions are exposed.
 */
public expect object Syscall {
    /**
     * Copies [size] bytes from the pointer at [pointer] to [buf].
     *
     * This uses memcpy() which is faster than the naiive Kotlin method. This will buffer
     * overflow if [pointer] is smaller than size!
     */
    @Unsafe
    public fun __fast_ptr_to_bytearray(pointer: COpaquePointer, buf: ByteArray, size: Int)
}
