/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Using the specified [Closeable], runs the lambda [block] and automatically closes the object
 * afterwards.
 */
@OptIn(ExperimentalContracts::class)
public inline fun <T : Closeable, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    try {
        return block(this)
    } finally {
        close()
    }
}

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
