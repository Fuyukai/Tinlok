/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.types

import tf.lotte.cc.Unsafe

// TODO: This is not very efficient.
//  I believe a proper search alg would be faster. But that's for another day.
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
 * Creates a substring of the specified range.
 */
@OptIn(Unsafe::class)
public fun ByteString.substring(start: Int, end: Int = size): ByteString {
    return ByteString.fromUncopied(unwrap().copyOfRange(start, end))
}

/**
 * Joins an iterable of [ByteString] together with the specified [delim].
 */
@OptIn(Unsafe::class)
public fun Collection<ByteString>.join(delim: ByteString): ByteString {
    val size = (delim.size * (this.size - 1)) + this.sumBy { it.size }
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
        // and we don't want a trailing delimiter
        if (it.hasNext()) {
            for (b in delim) {
                final[cursor] = b
                cursor += 1
            }
        }
    }

    return ByteString.fromUncopied(final)
}

private val HEX_ALPHABET =
    arrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    )

/**
 * Creates a new hex-encoded string from this ByteString.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteString.hexlify(): String {
    if (isEmpty()) return ""

    val buf = StringBuilder(this.size * 2)
    for (byte in this) {
        val ubyte = byte.toUByte()
        val upper = HEX_ALPHABET[((ubyte and 0xF0u).toInt()).ushr(4)]
        val lower = HEX_ALPHABET[(ubyte and 0x0Fu).toInt()]
        buf.append(upper)
        buf.append(lower)
    }

    return buf.toString()
}

internal fun Char.toInt16(): Int {
    return when (this) {
        'a', 'A' -> 10
        'b', 'B' -> 11
        'c', 'C' -> 12
        'd', 'D' -> 13
        'e', 'E' -> 14
        'f', 'F' -> 15
        else -> toInt()  // real numbers
    }
}

/**
 * Decodes a hex-encoded string from this String.
 */
@OptIn(Unsafe::class)
public fun String.unhexlify(): ByteString {
    require((length.rem(2)) == 0) { "Hex string length was not a multiple of two!" }
    val buf = ByteArray(length / 2)
    var cursor: Int = 0

    val iterator = this.iterator()
    while (true) {
        if (!iterator.hasNext()) break
        val first = iterator.next().toInt16().shl(4)
        val second = iterator.next().toInt16()

        val byte = (first or second).toByte()
        buf[cursor] = byte
        cursor += 1
    }

    return ByteString.fromUncopied(buf)
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
