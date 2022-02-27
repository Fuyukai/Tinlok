/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io

/**
 * Interface for any object that is readable.
 */
public interface Readable {
    /**
     * Reads no more than [size] amount of bytes into [ba], starting at [offset], returning the
     * number of bytes actually written.
     */
    public fun readInto(ba: ByteArray, size: Int = ba.size, offset: Int = 0): Int

    /**
     * Reads [size] amount of bytes into the specified [buffer], returning the number of bytes
     * actually read.
     */
    public fun readInto(buffer: Buffer, size: Int = buffer.capacity.toInt()): Int
}
