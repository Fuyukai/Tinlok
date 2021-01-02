/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.lotte.tinlok.codec

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.util.ByteString
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Table of base64 characters */
private val BASE64_TABLE: CharArray = charArrayOf(
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
    'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
    'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
    'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
    's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
    '3', '4', '5', '6', '7', '8', '9', '+', '/'
)

/** Table of urlsafe base64 characters */
private val BASE64_TABLE_URLSAFE: CharArray = charArrayOf(
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
    'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
    'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
    'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
    's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
    '3', '4', '5', '6', '7', '8', '9', '-', '_'
)

/** Padding character */
private const val PADDING = '='

/**
 * Encodes a three-chunk of base64. This is the most common operation.
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun encodeBase64ThreeChunk(
    table: CharArray, bs: ByteString, baset: Int, output: CharArray, outset: Int,
) {
    var baseOffset = baset
    // gross increment hacks
    val i1 = bs[baseOffset++].toUInt().shl(16)
    val i2 = bs[baseOffset++].toUInt().shl(8)
    val i3 = bs[baseOffset].toUInt()
    val iAll = i1.or(i2).or(i3)

    var outOffset = outset
    output[outOffset++] = table[(iAll.shr(18).and(0x3fu)).toInt()]
    output[outOffset++] = table[(iAll.shr(12).and(0x3fu)).toInt()]
    output[outOffset++] = table[(iAll.shr(6).and(0x3fu)).toInt()]
    output[outOffset] = table[(iAll.and(0x3fu)).toInt()]
}

/**
 * Encodes a two-chunk of base64.
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun encodeBase64TwoChunk(
    table: CharArray, bs: ByteString, baset: Int, output: CharArray, outset: Int,
) {
    var baseOffset = baset

    // more gross increment hacks
    // this is the same as threeChunk but the lower bits are all zero
    // and we just forcibly add a padding char
    val i1 = bs[baseOffset++].toUInt().shl(16)
    val i2 = bs[baseOffset].toUInt().shl(8)
    val iAll = i1.or(i2)

    var outOffset = outset
    output[outOffset++] = table[(iAll.shr(18).and(0x3fu)).toInt()]
    output[outOffset++] = table[(iAll.shr(12).and(0x3fu)).toInt()]
    output[outOffset++] = table[(iAll.shr(6).and(0x3fu)).toInt()]
    output[outOffset] = PADDING
}

/**
 * Encodes a singular byte into base64.
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun encodeBase64OneChunk(
    table: CharArray, b: Byte, output: CharArray, outset: Int,
) {
    val i = b.toUInt().shl(16)
    var outOffset = outset

    // basically the same as the two-chunk mechanism
    output[outOffset++] = table[(i.shr(18).and(0x3fu)).toInt()]
    output[outOffset++] = table[(i.shr(12).and(0x3fu)).toInt()]
    output[outOffset++] = PADDING
    output[outOffset] = PADDING
}

private fun roundUp(numToRound: Int, multiple: Int): Int {
    return (numToRound + multiple - 1) / multiple * multiple
}

/**
 * Encodes a [ByteString] into a base64 String.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public fun base64Encode(bs: ByteString, urlSafe: Boolean = false): String {
    // pre-allocate the chararray with the right size
    var size = ceil((bs.size.toFloat() * 8) / 6).roundToInt()
    size = roundUp(size, 4)

    // bytestring cursors
    var bsCursor = 0

    // output arr and cursor
    val ca = CharArray(size)
    var cursor = 0

    val alphabet = if (urlSafe) BASE64_TABLE_URLSAFE else BASE64_TABLE

    while (true) {
        // loose check to ensure we encode 2 and 1-blocks properly
        val remaining = bs.size - bsCursor
        if (remaining >= 3) {
            // 3 blocks are simplest as lcm(6, 8) == 24 so we can easily fit this into a uint32
            encodeBase64ThreeChunk(alphabet, bs, bsCursor, ca, cursor)
            // adjust cursors for next iterations
            bsCursor += 3
            cursor += 4
        } else if (remaining == 2) {
            // 2 blocks are encoded differently
            // and this means we're at the end of the bytestring so we can exit after this
            encodeBase64TwoChunk(alphabet, bs, bsCursor, ca, cursor)
            break
        } else if (remaining == 1) {
            // 1 blocks are also encoded differently
            val byte = bs[bsCursor]
            encodeBase64OneChunk(alphabet, byte, ca, cursor)
            break
        } else break
    }

    return ca.concatToString()
}

/**
 * Gets the real value of a base64 character.
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun realValueb64(char: Char): UInt {
    // why - 97 + 26?
    // because 97 is the ascii offset for 'a', and 26 is the base64 offset.
    // i can combine them, but a sufficiently smart kotlin compiler should be doing that for me.
    return when (char) {
        '+', '-' -> 62u
        '/', '_' -> 63u
        in 'A'..'Z' -> (char.toInt() - 65).toUInt()
        in 'a'..'z' -> (char.toInt() - 97 + 26).toUInt()
        else -> throw IllegalArgumentException("invalid char: $char")
    }
}

/**
 * Decodes a three-chunk of Base64 (four characters).
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun decodeBase64ThreeChunk(chunk: String, out: ByteArray, outset: Int) {
    // decompose individual hexets
    val i1 = realValueb64(chunk[0]).shl(18)
    val i2 = realValueb64(chunk[1]).shl(12)
    val i3 = realValueb64(chunk[2]).shl(6)
    val i4 = realValueb64(chunk[3])
    val iAll = i1.or(i2).or(i3).or(i4)

    var offset = outset
    out[offset++] = (iAll.shr(16)).and(0xffu).toByte()
    out[offset++] = (iAll.shr(8)).and(0xffu).toByte()
    out[offset] = (iAll.and(0xffu)).toByte()
}

/**
 * Decodes a two-chunk of Base64 (three chars and one padding)
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun decodeBase64TwoChunk(chunk: String, out: ByteArray, outset: Int) {
    val i1 = realValueb64(chunk[0]).shl(18)
    val i2 = realValueb64(chunk[1]).shl(12)
    val i3 = realValueb64(chunk[2]).shl(6)
    val iAll = i1.or(i2).or(i3)

    var offset = outset
    out[offset++] = (iAll.shr(16).and(0xffu)).toByte()
    out[offset] = (iAll.shr(8).and(0xffu)).toByte()
}

/**
 * Decodes a one-chunk of base64 (two chars and one padding).
 */
@OptIn(ExperimentalUnsignedTypes::class)
private fun decodeBase64OneChunk(chunk: String, out: ByteArray, outset: Int) {
    val i1 = realValueb64(chunk[0]).shl(18)
    val i2 = realValueb64(chunk[1]).shl(12)
    val iAll = i1.or(i2)

    out[outset] = (iAll.shr(16).and(0xffu)).toByte()
}

/**
 * Decodes a String containing data encoded in base64 into a [ByteString].
 *
 * The string must be correctly padded.
 */
@OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
public fun base64Decode(str: String): ByteString {
    require(str.length.rem(4) == 0) { "String is incorrectly padded" }
    // calculate final length
    var size = (str.length * 6) / 8

    // strip size according to ending padding
    if (str.endsWith("==")) size -= 2
    else if (str.endsWith('=')) size -= 1

    // final array and cursor
    val output = ByteArray(size)
    var outCursor = 0

    // slow impl using chunked, but i do not care.
    for (chunk in str.chunkedSequence(4)) {
        when {
            chunk.endsWith("==") -> {
                decodeBase64OneChunk(chunk, output, outCursor)
            }
            chunk.endsWith("=") -> {
                decodeBase64TwoChunk(chunk, output, outCursor)
            }
            else -> {
                decodeBase64ThreeChunk(chunk, output, outCursor)
                outCursor += 3
            }
        }
    }

    return ByteString.fromUncopied(output)
}

// HLA
/**
 * Encodes this [ByteString] using Base64.
 */
public fun ByteString.encodeBase64(urlSafe: Boolean = false): String {
    return base64Encode(this, urlSafe = urlSafe)
}

/**
 * Decodes this [String] using Base64.
 */
public fun String.decodeBase64(): ByteString {
    return base64Decode(this)
}
