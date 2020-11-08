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
 * Interface for any object that is readable.
 */
public interface Readable {
    /**
     * Reads no more than [size] amount of bytes into [buf], starting at [offset], returning the
     * number of bytes actually written.
     */
    public fun readInto(buf: ByteArray, size: Int = buf.size, offset: Int = 0): Int
}
