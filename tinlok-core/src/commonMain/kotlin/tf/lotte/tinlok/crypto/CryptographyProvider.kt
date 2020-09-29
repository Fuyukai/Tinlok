/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.util.Unsafe

/**
 * Low-level cryptography provider interface. Allows actually acquiring hasher and cipher instances.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect interface CryptographyProvider {
    public companion object {
        /**
         * Gets the current [CryptographyProvider].
         */
        public fun current(): CryptographyProvider

        /**
         * Sets the current [CryptographyProvider].
         */
        public fun change(provider: CryptographyProvider)
    }

    /** The default integrity hashing algorithm. */
    public val defaultIntegrityHashAlgorithm: String

    /**
     * Gets an [IntegrityHasher] instance, or null if the specified algorithm was not found.
     *
     * This is marked as Unsafe as integrity hashers may leak resources.
     */
    @Unsafe
    public fun getIntegrityHasher(algorithm: String): IntegrityHasher?

    /**
     * Securely compares two byte arrays. Used for hash comparisons.
     */
    public fun secureCompare(first: UByteArray, second: UByteArray): Boolean
}
