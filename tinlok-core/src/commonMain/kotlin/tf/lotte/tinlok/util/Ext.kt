/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import kotlin.random.Random


/**
 * Converts this short array into a string (assuming it is UTF-16).
 */
public fun ShortArray.utf16ToString(count: Int = this.size): String {
    val ca = CharArray(count)
    for (idx in 0 until count) {
        ca[idx] = this[idx].toChar()
    }
    return ca.concatToString()
}

/**
 * Converts this UShortArray into a string (assumiing it is UTF-16).
 */
@ExperimentalUnsignedTypes
public fun UShortArray.utf16ToString(count: Int = this.size): String {
    val ca = CharArray(count)
    for (idx in 0 until count) {
        ca[idx] = this[idx].toShort().toChar()
    }
    return ca.concatToString()
}

/**
 * Generates a random ASCII string of the specified [size].
 */
public fun Random.randomAsciiString(size: Int): String {
    val arr = CharArray(size)

    for (idx in 0 until size) {
        val isHigher = nextBoolean()
        val i = if (isHigher) {
            nextInt(0x41, 0x5b)
        } else {
            nextInt(0x61, 0x7b)
        }
        val char = i.toChar()
        arr[idx] = char
    }

    return arr.concatToString()
}