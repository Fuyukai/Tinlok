/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.fs.SynchronousFile
import tf.veriny.tinlok.util.ByteString

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
    return writeFrom(unwrapped)
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

/**
 * Reads all the bytes from this file.
 */
@OptIn(Unsafe::class)
public fun SynchronousFile.readAll(): ByteString {
    // TODO: We can do this better with some sort of linked list chunked array implementation.

    // ensure that we read the size of the real file, not the size of the symlink.
    val realPath = path.resolveFully(strict = true)
    val size = realPath.size() - cursor()
    if (size >= Int.MAX_VALUE) {
        throw UnsupportedOperationException("File is too big to read in one go")
    }

    val ba = ByteArray(size.toInt())
    val read = readInto(ba)
    return if (read == size.toInt()) {
        ByteString.fromUncopied(ba)
    } else {
        // need to reallocate...
        val newBa = ba.copyOfRange(0, read)
        ByteString.fromUncopied(newBa)
    }
}
