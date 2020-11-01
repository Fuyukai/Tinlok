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

// Number --> ByteArray
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

// ByteArray --> Number

/**
 * Decodes the two bytes in this ByteArray from [offset] onwards into a single short value in big
 * endian mode.
 */
public inline fun ByteArray.toShort(offset: Int = 0): Short {
    val i1 = (this[offset].toInt().shl(16))
    val i2 = (this[offset + 1].toInt())
    return (i1.or(i2)).toShort()
}

/**
 * Decodes the two bytes in this ByteArray from [offset] onwards into a single short value in
 * little endian mode.
 */
public inline fun ByteArray.toShortLE(offset: Int = 0): Short {
    val i1 = (this[offset + 1].toInt().shl(16))
    val i2 = (this[offset].toInt())
    return (i1.or(i2)).toShort()
}

/**
 * Decodes a size-4 byte array to an int in big endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toInt(offset: Int = 0): Int {
    return (((this[offset].toInt()) shl 24)
        or ((this[offset + 1].toInt()) shl 16)
        or ((this[offset + 2].toInt()) shl 8)
        or (this[offset + 3].toInt())
        )
}

/**
 * Decodes a size-4 byte array to an int in little endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toIntLE(offset: Int = 0): Int {
    return (((this[offset + 3].toInt()) shl 24)
        or ((this[offset + 2].toInt()) shl 16)
        or ((this[offset + 1].toInt()) shl 8)
        or (this[offset].toInt())
        )
}

/**
 * Decodes a size-8 byte array to a long in big endian mode.
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
 * Decodes a size-8 byte array to a long in little endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toLongLE(offset: Int = 0): Long {
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
 * Decodes a size-4 unsigned byte array to a uint in big endian mode.
 */
/* @InlineOnly */
public inline fun UByteArray.toUInt(offset: Int = 0): UInt {
    return (((this[offset].toUInt()) shl 24)
        or ((this[offset + 1].toUInt()) shl 16)
        or ((this[offset + 2].toUInt()) shl 8)
        or (this[offset + 3].toUInt())
        )
}

/**
 * Decodes a size-4 unsigned byte array to a uint in big endian mode.
 */
/* @InlineOnly */
public inline fun UByteArray.toUIntLE(offset: Int = 0): UInt {
    return (((this[offset + 3].toUInt()) shl 24)
        or ((this[offset + 2].toUInt()) shl 16)
        or ((this[offset + 1].toUInt()) shl 8)
        or (this[offset].toUInt())
        )
}

/**
 * Decodes a size-8 byte array to a ulong in big endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toULong(offset: Int = 0): ULong {
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
 * Decodes a size-8 byte array to a ulong in little endian mode.
 */
/* @InlineOnly */
public inline fun ByteArray.toULongLE(offset: Int = 0): ULong {
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
