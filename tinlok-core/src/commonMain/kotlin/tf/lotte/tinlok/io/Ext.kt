/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.util.ByteString

/**
 * Reads no more than [count] bytes from this object.
 *
 * A null return means EOF.
 */
@OptIn(Unsafe::class)
public fun Readable.readUpTo(count: Int): ByteString? {
    val buf = ByteArray(count.toInt())
    val read = readInto(buf)

    return if (read == 0) null
    else {
        if (read == count) ByteString.fromUncopied(buf)
        else ByteString.fromUncopied(buf.copyOfRange(0, read))
    }
}

/**
 * Writes the entirety of the ByteString [bs] into the specified buffer.
 */
@OptIn(Unsafe::class)
public fun Writeable.writeAll(bs: ByteString): Int {
    val unwrapped = bs.unwrap()
    return writeAllFrom(unwrapped)
}

/**
 * Peeks no more than the specified number of bytes without advancing the cursor position.
 */
public fun <T> T.peek(count: Int): ByteString?
    where T : Readable, T : Seekable {
    val cursorBefore = cursor()
    val bs = readUpTo(count) ?: return null
    this.seekAbsolute(cursorBefore)
    return bs
}


