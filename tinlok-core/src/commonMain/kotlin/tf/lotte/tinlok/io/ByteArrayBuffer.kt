/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

/**
 * A [Buffer] that wraps a [ByteArray].
 */
public expect class ByteArrayBuffer
constructor(ba: ByteArray) : Buffer {
    public companion object;
}

/**
 * Creates a new [ByteArrayBuffer] of the specified [size], initialised to zero.
 */
public fun ByteArrayBuffer.Companion.ofSize(size: Int): ByteArrayBuffer {
    val ba = ByteArray(size){ 0 }
    return ByteArrayBuffer(ba)
}
