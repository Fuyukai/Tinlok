/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("NOTHING_TO_INLINE")

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

/**
 * An array corresponding to the hex alphabet.
 */
public val HEX_ALPHABET: Array<Char> =
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

/**
 * Converts a hexadecimal char into an [Int].
 */
@Suppress("ConvertTwoComparisonsToRangeCheck")
public fun Char.toIntHex(): Int {
    return if (this >= 'a' && this <= 'f') {
        this.toInt() - 87  // 'a' is ordinal 97
    } else if (this >= 'A' && this <= 'F') {
        toInt() - 55  // 'f' is ordinal 65
    } else if (this >= '0' && this <= '9') {
        toInt() - 48  // '0' is ordinal 48
    } else {
        throw IllegalArgumentException("Not a hexadecimal digit: $this")
    }

}

/**
 * Decodes a hex-encoded string from this String.
 */
@OptIn(Unsafe::class)
public fun String.unhexlify(): ByteString {
    require((length.rem(2)) == 0) { "Hex string length was not a multiple of two!" }
    val buf = ByteArray(length / 2)
    var cursor = 0

    val iterator = this.iterator()
    while (true) {
        if (!iterator.hasNext()) break
        val first = iterator.next().toIntHex().shl(4)
        val second = iterator.next().toIntHex()

        val byte = (first or second).toByte()
        buf[cursor] = byte
        cursor += 1
    }

    return ByteString.fromUncopied(buf)
}

// ByteString -> Numbers

/**
 * Decodes the bytes in this ByteString to a short in big endian mode, starting from [offset].
 */
public inline fun ByteString.toShort(offset: Int = 0): Short {
    val i1 = (this[offset].toInt().shl(16))
    val i2 = (this[offset + 1].toInt())
    return (i1.or(i2)).toShort()
}

/**
 * Decodes the bytes in this ByteString to an short in big endian mode, starting from [offset].
 */
public inline fun ByteString.toShortLE(offset: Int = 0): Short {
    val i1 = (this[offset + 1].toInt().shl(16))
    val i2 = (this[offset].toInt())
    return (i1.or(i2)).toShort()
}

/**
 * Decodes the bytes in this ByteString to an int in big endian mode, starting from [offset].
 */
/* @InlineOnly */
public inline fun ByteString.toInt(offset: Int = 0): Int {
    return (((this[offset].toInt()) shl 24)
        or ((this[offset + 1].toInt()) shl 16)
        or ((this[offset + 2].toInt()) shl 8)
        or (this[offset + 3].toInt())
        )
}

/**
 * Decodes the bytes in this ByteString to an int in little endian mode, starting from [offset].
 */
/* @InlineOnly */
public inline fun ByteString.toIntLE(offset: Int = 0): Int {
    return (((this[offset + 3].toInt()) shl 24)
        or ((this[offset + 2].toInt()) shl 16)
        or ((this[offset + 1].toInt()) shl 8)
        or (this[offset].toInt())
        )
}

/**
 * Decodes the bytes in this ByteString to a long in big endian mode, starting from [offset].
 */
/* @InlineOnly */
public inline fun ByteArray.toLong(offset: Int = 0): Long {
    return (((this[offset].toLong()) shl 56)
        or ((this[offset + 1].toLong()) shl 48)
        or ((this[offset + 2].toLong()) shl 40)
        or ((this[offset + 3].toLong()) shl 32)
        or ((this[offset + 4].toLong()) shl 24)
        or ((this[offset + 5].toLong()) shl 16)
        or ((this[offset + 6].toLong()) shl 8)
        or (this[offset + 7].toLong())
        )
}

/**
 * Decodes the bytes in this ByteString to a long in little endian mode, starting from [offset].
 */
/* @InlineOnly */
public inline fun ByteString.toLongLE(offset: Int = 0): Long {
    return (((this[offset + 7].toLong()) shl 56)
        or ((this[offset + 6].toLong()) shl 48)
        or ((this[offset + 5].toLong()) shl 40)
        or ((this[offset + 4].toLong()) shl 32)
        or ((this[offset + 3].toLong()) shl 24)
        or ((this[offset + 2].toLong()) shl 16)
        or ((this[offset + 1].toLong()) shl 8)
        or (this[0].toLong())
        )
}

/**
 * Decodes the bytes in this ByteString to an unsigned long in big endian mode, starting from
 * [offset].
 */
/* @InlineOnly */
@OptIn(ExperimentalUnsignedTypes::class)
public inline fun ByteString.toULong(offset: Int = 0): ULong {
    return (((this[offset].toULong()) shl 56)
        or ((this[offset + 1].toULong()) shl 48)
        or ((this[offset + 2].toULong()) shl 40)
        or ((this[offset + 3].toULong()) shl 32)
        or ((this[offset + 4].toULong()) shl 24)
        or ((this[offset + 5].toULong()) shl 16)
        or ((this[offset + 6].toULong()) shl 8)
        or (this[offset + 7].toULong())
        )
}

/**
 * Decodes the bytes in this ByteString to an unsigned long in little endian mode, starting from
 * [offset].
 */
/* @InlineOnly */
@OptIn(ExperimentalUnsignedTypes::class)
public inline fun ByteString.toULongLE(offset: Int = 0): ULong {
    return (((this[offset + 7].toULong()) shl 56)
        or ((this[offset + 6].toULong()) shl 48)
        or ((this[offset + 5].toULong()) shl 40)
        or ((this[offset + 4].toULong()) shl 32)
        or ((this[offset + 3].toULong()) shl 24)
        or ((this[offset + 2].toULong()) shl 16)
        or ((this[offset + 1].toULong()) shl 8)
        or (this[offset].toULong())
        )
}

/**
 * Creates a new [ByteString] from this [String].
 */
public fun String.toByteString(): ByteString = ByteString.fromString(this)

/**
 * Creates a new [ByteString] from this [ByteArray].
 */
public fun ByteArray.toByteString(): ByteString = ByteString.fromByteArray(this)

// uncopied because toByteArray copies it.
/**
 * Creates a new [ByteString] from this [UByteArray].
 */
@OptIn(Unsafe::class, ExperimentalUnsignedTypes::class)
public fun UByteArray.toByteString(): ByteString = ByteString.fromUncopied(toByteArray())

/**
 * Creates a new [ByteString] from this collection of bytes.
 */
@OptIn(Unsafe::class)
public fun Collection<Byte>.toByteString(): ByteString = ByteString.fromUncopied(toByteArray())

/**
 * Creates a [ByteString] filled with zeroes.
 */
@OptIn(Unsafe::class)
public fun ByteString.Companion.zeroed(size: Int): ByteString {
    val ba = ByteArray(size)
    return fromUncopied(ba)
}

/**
 * Helper function for ByteString, similar to Python b"abc".
 */
public inline fun b(s: String): ByteString = s.toByteString()
