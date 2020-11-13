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
 * Represents any object that is writeable into.
 */
public interface Writeable {
    /**
     * Attempts to write all [size] bytes of the ByteArray [ba] to this object, starting from
     * [offset], returning the number of bytes actually written before reaching EOF.
     *
     * This method will attempt retries to write all of the specified bytes.
     */
    public fun writeFrom(ba: ByteArray, size: Int = ba.size, offset: Int = 0): Int

    /**
     * Attempts to write [size] bytes of [buffer] from the cursor onwards to this object,
     * returning the number of bytes actually written before reaching EOF.
     *
     * This method will attempt retries to write all of the specified bytes.
     */
    public fun writeFrom(buffer: Buffer, size: Int = buffer.remaining.toInt()): Int
}
