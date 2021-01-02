/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.util.ByteString
import tf.lotte.tinlok.util.hexlify

/**
 * Wraps the raw bytes of an outgoing Blake2b hash. This is a 64-byte hash (i.e. a BLAKE2-512 hash).
 */
public inline class Blake2bHash(public val bytes: ByteString) {
    /**
     * Creates a hex digest for this hash.
     */
    public fun hexdigest(): String {
        return bytes.hexlify()
    }

    /**
     * Securely compares this hash with another hash.
     */
    @OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
    public fun secureCompare(other: Blake2bHash): Boolean {
        val first = bytes.unwrap().toUByteArray()
        val second = other.bytes.unwrap().toUByteArray()
        return crypto_verify64(first, second)
    }
}
