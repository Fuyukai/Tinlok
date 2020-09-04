/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste

import kotlin.jvm.JvmName

/**
 * Splits a [ByteString] by the specified [delim].
 */
public fun ByteString.split(delim: ByteString): List<ByteString> {
    // final output
    val working = ArrayList<ByteString>(size / delim.size)
    // current processing
    val current = ByteArray(size)
    // pointer to the head of the current processing
    var currentCursor = 0
    // matched count to the delimiter, used to chop the tail off
    var matched = 0

    // cool function!
    for (byt in this) {
        // always copy the byte to the current working array
        current[currentCursor] = byt
        currentCursor += 1

        // delim check
        if (byt == delim[matched]) {
            matched += 1
        } else {
            matched = 0
        }

        // if the match count is the same as the delim, we have fully matched the delimiter
        // in which case, we chop off the delimiter, then copy the working array
        if (matched == delim.size) {
            val copy = current.copyOfRange(0, currentCursor - matched).toByteString()
            currentCursor = 0
            matched = 0
            current.fill(0)

            working.add(copy)
        }
    }

    // exited the loop, add anything left in the current cursor to the list
    // (as it didn't match fully)
    if (currentCursor > 0) {
        val copy = current.copyOfRange(0, currentCursor).toByteString()
        working.add(copy)
    }
    return working
}

/**
 * Joins an iterable of [ByteString] together with the specified [delim].
 */
public fun Iterable<ByteString>.join(delim: ByteString): ByteString {
    val size = delim.size + this.sumBy { it.size }
    val final = ByteArray(size)
    var cursor = 0

    val it = iterator()

    for (part in it) {
        for (b in part) {
            final[cursor] = b
            cursor += 1
        }

        // clever check for the last item
        // hasNext() will return false if this is the last one
        // and we don't want a trailing slash
        if (it.hasNext()) {
            for (b in delim) {
                final[cursor] = b
                cursor += 1
            }
        }
    }

    return ByteString.fromByteArray(final)
}


/**
 * Creates a new [ByteString] from this [String].
 */
public fun String.toByteString(): ByteString = ByteString.fromString(this)

/**
 * Creates a new [ByteString] from this [ByteArray].
 */
public fun ByteArray.toByteString(): ByteString = ByteString.fromByteArray(this)

/**
 * Helper function for ByteString, similar to Python b"abc".
 */
public inline fun b(s: String): ByteString = s.toByteString()
