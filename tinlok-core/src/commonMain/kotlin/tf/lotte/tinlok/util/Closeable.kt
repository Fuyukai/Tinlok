/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
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
 * Represents any object that is closeable.
 */
public fun interface Closeable {
    /**
     * Closes this resource.
     *
     * This method is idempotent; subsequent calls will have no effects.
     */
    public fun close()
}

/**
 * Extension function that runs [block] and then automatically closes this [Closeable].
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
