/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.ByteString
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// misc extensions
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
 * Peeks no more than the specified number of bytes without advancing the cursor position.
 */
public fun <T> T.peek(count: Long): ByteString?
    where T : Readable, T : Seekable {
    val cursorBefore = cursor()
    val bs = readUpTo(count) ?: return null
    this.seekAbsolute(cursorBefore)
    return bs
}


