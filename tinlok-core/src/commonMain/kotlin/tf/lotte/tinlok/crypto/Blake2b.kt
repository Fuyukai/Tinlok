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

import tf.lotte.tinlok.io.Closeable
import tf.lotte.tinlok.io.use
import tf.lotte.tinlok.types.bytestring.ByteString

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
public operator fun <R> Blake2b.Companion.invoke(
    key: UByteArray = ubyteArrayOf(),
    block: (Blake2b) -> R): R
{
    val hasher = Blake2b(key)
    return hasher.use(block)
}
