/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)
package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.util.Closeable
import tf.lotte.tinlok.util.ClosingScope
import tf.lotte.tinlok.util.use

/**
 * A class that takes in data and will eventually produce a hash using the Blake2b algorithm.
 */
public expect class Blake2b internal constructor(key: UByteArray) : Closeable {
    public companion object;

    /**
     * Feeds some data into this object.
     */
    public fun feed(data: ByteString)

    /**
     * Performs the Blake2b hashing algorithm over all previously consumed data and returns a
     * final hash.
     */
    public fun hash(): Blake2bHash
}

/**
 * Creates a new [Blake2b] instance with the specified [key], passing it to the specified [block].
 */
@OptIn(ExperimentalUnsignedTypes::class)
public operator fun <R> Blake2b.Companion.invoke(
    key: UByteArray = ubyteArrayOf(),
    block: (Blake2b) -> R): R
{
    val hasher = Blake2b(key)
    return hasher.use(block)
}

/**
 * Creates a new [Blake2b] instance with the specified [key], adding it to the specified closing
 * [scope].
 */
@OptIn(ExperimentalUnsignedTypes::class)
public operator fun Blake2b.Companion.invoke(
    scope: ClosingScope, key: UByteArray = ubyteArrayOf()
): Blake2b {
    val hasher = Blake2b(key)
    scope.add(hasher)
    return hasher
}

/**
 * Creates a [Blake2bHash] for the contents of this [ByteString].
 */
@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteString.blake2b(key: UByteArray = ubyteArrayOf()): Blake2bHash = Blake2b {
    it.feed(this)
    it.hash()
}
