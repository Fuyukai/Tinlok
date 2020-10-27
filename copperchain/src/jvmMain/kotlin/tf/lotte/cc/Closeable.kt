/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

// needed to generate the right empty file due to how typealiases work
@file:JvmName("__CloseableKt")
package tf.lotte.cc

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// simple typealias for AutoCloseable, as it has the same semantics
public actual typealias Closeable = AutoCloseable

@OptIn(ExperimentalContracts::class)
public actual inline fun <T : Closeable, R> T.use(block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    try {
        return block(this)
    } finally {
        close()
    }
}
