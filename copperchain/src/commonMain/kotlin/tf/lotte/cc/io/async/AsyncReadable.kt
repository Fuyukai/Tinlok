/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.io.async

import tf.lotte.cc.io.Readable

/**
 * An asynchronous version of [Readable].
 */
public interface AsyncReadable {
    /**
     * Reads no more than [size] amount of bytes into [buf], starting at [offset], returning the
     * number of bytes actually written.
     *
     * This will suspend until *some* data is available.
     */
    public suspend fun readInto(buf: ByteArray, offset: Int = 0, size: Int = buf.size): Int
}
