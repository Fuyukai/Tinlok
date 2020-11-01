/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("unused")

package tf.lotte.cc.util


// safe conversion
/**
 * Gets the upper byte of this Int.
 */
/* @InlineOnly */
public inline val Int.upperByte: Int
    get() = (this ushr 24) and 0xFF

/**
 * Gets the upper byte of this UInt.
 */
/* @InlineOnly */
public inline val UInt.upperByte: UInt
    get() = (this shr 24) and 0xFFu

/**
 * Gets the lower byte of this Int.
 */
/* @InlineOnly */
public inline val Int.lowerByte: Int
    get() = this and 0xFF

/**
 * Gets the lower byte of this UInt.
 */
/* @InlineOnly */
public inline val UInt.lowerByte: UInt
    get() = this and 0xFFu

/**
 * Gets the second byte of this Int.
 */
/* @InlineOnly */
public inline val Int.byte2: Int
    get() = (this ushr 16) and 0xFF


/**
 * Gets the second byte of this UInt.
 */
/* @InlineOnly */
public inline val UInt.byte2: UInt
    get() = (this shr 16) and 0xFFu


/**
 * Gets the third byte of this Int.
 */
/* @InlineOnly */
public inline val Int.byte3: Int
    get() = (this ushr 8) and 0xFF

/**
 * Gets the third byte of this UInt.
 */
/* @InlineOnly */
public inline val UInt.byte3: UInt
    get() = (this shr 8) and 0xFFu


/**
 * Decodes this int into a [ByteArray] in big endian mode.
 */
/* @InlineOnly */
public inline fun Int.toByteArray(): ByteArray {
    return byteArrayOf(upperByte.toByte(), byte2.toByte(), byte3.toByte(), lowerByte.toByte())
}

/**
 * Decodes this int into a [ByteArray] in little endian mode.
 */
/* @InlineOnly */
public inline fun Int.toByteArrayLE(): ByteArray {
    return byteArrayOf(lowerByte.toByte(), byte3.toByte(), byte2.toByte(), upperByte.toByte())
}

/**
 * Decodes this uint into a ByteArray in big endian mode.
 */
/* @InlineOnly */
@OptIn(ExperimentalUnsignedTypes::class)  // ?
public inline fun UInt.toByteArray(): ByteArray {
    return byteArrayOf(upperByte.toByte(), byte2.toByte(), byte3.toByte(), lowerByte.toByte())
}

/**
 * Decodes this int into a [ByteArray] in little endian mode.
 */
/* @InlineOnly */
public inline fun UInt.toByteArrayLE(): ByteArray {
    return byteArrayOf(lowerByte.toByte(), byte3.toByte(), byte2.toByte(), upperByte.toByte())
}

/**
 * Decodes this long into a [ByteArray] in big endian mode.
 */
/* @InlineOnly */
public inline fun Long.toByteArray(): ByteArray {
    return byteArrayOf(
        ((this ushr 56) and 0xffL).toByte(),
        ((this ushr 48) and 0xffL).toByte(),
        ((this ushr 40) and 0xffL).toByte(),
        ((this ushr 32) and 0xffL).toByte(),
        ((this ushr 24) and 0xffL).toByte(),
        ((this ushr 16) and 0xffL).toByte(),
        ((this ushr  8) and 0xffL).toByte(),
        ((this        ) and 0xffL).toByte()
    )
}

/**
 * Decodes this long into a [ByteArray] in little endian mode.
 */
/* @InlineOnly */
public inline fun Long.toByteArrayLE(): ByteArray {
    return byteArrayOf(
        ((this        ) and 0xffL).toByte(),
        ((this ushr  8) and 0xffL).toByte(),
        ((this ushr 16) and 0xffL).toByte(),
        ((this ushr 24) and 0xffL).toByte(),
        ((this ushr 32) and 0xffL).toByte(),
        ((this ushr 40) and 0xffL).toByte(),
        ((this ushr 48) and 0xffL).toByte(),
        ((this ushr 56) and 0xffL).toByte()
    )
}

/**
 * Decodes this ulong into a [ByteArray] in big endian mode.
 */
public inline fun ULong.toByteArray(): ByteArray {
    return byteArrayOf(
        ((this shr 56) and 0xffUL).toByte(),
        ((this shr 48) and 0xffUL).toByte(),
        ((this shr 40) and 0xffUL).toByte(),
        ((this shr 32) and 0xffUL).toByte(),
        ((this shr 24) and 0xffUL).toByte(),
        ((this shr 16) and 0xffUL).toByte(),
        ((this shr  8) and 0xffUL).toByte(),
        ((this       ) and 0xffUL).toByte()
    )
}

/**
 * Decodes this long into a [ByteArray] in little endian mode.
 */
/* @InlineOnly */
public inline fun ULong.toByteArrayLE(): ByteArray {
    return byteArrayOf(
        ((this       ) and 0xffUL).toByte(),
        ((this shr  8) and 0xffUL).toByte(),
        ((this shr 16) and 0xffUL).toByte(),
        ((this shr 24) and 0xffUL).toByte(),
        ((this shr 32) and 0xffUL).toByte(),
        ((this shr 40) and 0xffUL).toByte(),
        ((this shr 48) and 0xffUL).toByte(),
        ((this shr 56) and 0xffUL).toByte()
    )
}


// wtf past me
// this was this[0], this[1], this[0], this[0]
/**
 * Decodes a size-4 byte array to an int in big endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toInt(): Int {
    return (((this[0].toInt()) shl 24)
        or ((this[1].toInt()) shl 16)
        or ((this[2].toInt()) shl 8)
        or (this[3].toInt())
        )
}

/**
 * Decodes a size-4 byte array to an int in little endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toIntLE(): Int {
    return (((this[3].toInt()) shl 24)
        or ((this[2].toInt()) shl 16)
        or ((this[1].toInt()) shl 8)
        or (this[0].toInt())
        )
}

/**
 * Decodes a size-8 byte array to a long in big endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toLong(): Long {
    return (((this[0].toLong()) shl 56)
        or ((this[1].toLong()) shl 48)
        or ((this[2].toLong()) shl 40)
        or ((this[3].toLong()) shl 32)
        or ((this[4].toLong()) shl 24)
        or ((this[5].toLong()) shl 16)
        or ((this[6].toLong()) shl 8)
        or (this[7].toLong())
        )
}

/**
 * Decodes a size-8 byte array to a long in little endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toLongLE(): Long {
    return (((this[7].toLong()) shl 56)
        or ((this[6].toLong()) shl 48)
        or ((this[5].toLong()) shl 40)
        or ((this[4].toLong()) shl 32)
        or ((this[3].toLong()) shl 24)
        or ((this[2].toLong()) shl 16)
        or ((this[1].toLong()) shl 8)
        or (this[0].toLong())
        )
}

/**
 * Decodes a size-4 unsigned byte array to a uint in big endian mode.
 */
/* @InlineOnly */
public inline fun UByteArray.toUInt(): UInt {
    return (((this[0].toUInt()) shl 24)
        or ((this[1].toUInt()) shl 16)
        or ((this[2].toUInt()) shl 8)
        or (this[3].toUInt())
        )
}

/**
 * Decodes a size-4 unsigned byte array to a uint in big endian mode.
 */
/* @InlineOnly */
public inline fun UByteArray.toUIntLE(): UInt {
    return (((this[3].toUInt()) shl 24)
        or ((this[2].toUInt()) shl 16)
        or ((this[1].toUInt()) shl 8)
        or (this[0].toUInt())
        )
}

/**
 * Decodes a size-8 byte array to a ulong in big endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toULong(): ULong {
    return (((this[0].toULong()) shl 56)
        or ((this[1].toULong()) shl 48)
        or ((this[2].toULong()) shl 40)
        or ((this[3].toULong()) shl 32)
        or ((this[4].toULong()) shl 24)
        or ((this[5].toULong()) shl 16)
        or ((this[6].toULong()) shl 8)
        or (this[7].toULong())
        )
}

/**
 * Decodes a size-8 byte array to a ulong in little endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toULongLE(): ULong {
    return (((this[7].toULong()) shl 56)
        or ((this[6].toULong()) shl 48)
        or ((this[5].toULong()) shl 40)
        or ((this[4].toULong()) shl 32)
        or ((this[3].toULong()) shl 24)
        or ((this[2].toULong()) shl 16)
        or ((this[1].toULong()) shl 8)
        or (this[0].toULong())
        )
}

/**
 * Gets the upper byte of this Long.
 */
/* @InlineOnly */
public inline val Long.upperByte: Long
    get() = ((this.toULong()) and 0xFF00000000000000UL).toLong()

/**
 * Gets the lower byte of this Long.
 */
/* @InlineOnly */
public inline val Long.lowerByte: Long
    get() = (this and 0x00000000000000FF)


/**
 * Creates a new int with all of the specified [flag] values OR'd together.
 */
public fun flags(vararg flag: Int): Int {
    var acc = 0
    for (f in flag) {
        acc = acc.or(f)
    }
    return acc
}

/**
 * Creates a new uint with all of the specified [flag] values OR'd together.
 */
public fun flags(vararg flag: UInt): UInt {
    var acc = 0u
    for (f in flag) {
        acc = acc.or(f)
    }
    return acc
}

/**
 * Checks if [input] has bits from [flag] set.
 */
public fun flagged(input: Int, flag: Int): Boolean {
    return input.and(flag) != 0
}

/**
 * Checks if [input] has bits from [flag] set.
 */
public fun flagged(input: Int, flag: UInt): Boolean {
    return input.toUInt().and(flag) != 0u
}

/**
 * Checks if [input] has bits from [flag] set.
 */
public fun flagged(input: UInt, flag: Int): Boolean {
    return input.and(flag.toUInt()) != 0u
}

/**
 * Checks if [input] has bits from [flag] set.
 */
public fun flagged(input: UInt, flag: UInt): Boolean {
    return input.and(flag) != 0u
}