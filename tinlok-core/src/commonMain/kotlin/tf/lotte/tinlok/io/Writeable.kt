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
     * Attempts to write the entirety of the ByteArray [ba] to this object, returning the number of
     * bytes actually written before reaching EOF.
     */
    public fun writeAllFrom(ba: ByteArray): Int

    /**
     * Attempts to write the entirety of the buffer [buf] from the cursor onwards to this object,
     * returning the number of bytes actually written before reaching EOF.
     */
    public fun writeAllFrom(buffer: Buffer): Int
}
