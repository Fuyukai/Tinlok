/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.ByteString
import tf.lotte.tinlok.io.Closeable
import tf.lotte.tinlok.util.Unsafe

/**
 * A hasher that takes in data and produces an integrity hash over all of the consumed data.
 *
 * This class is a [Closeable]; closing it will discard the data within.
 */
public interface IntegrityHasher : Closeable {
    public companion object {
        /**
         * Gets a new instance of an [IntegrityHasher]. If [algorithm] is null, the default
         * algorithm will be used.
         */
        @Unsafe
        public fun unsafeGetInstance(algorithm: String? = null): IntegrityHasher {
            val provider = CryptographyProvider.current()
            val algo = algorithm ?: provider.defaultIntegrityHashAlgorithm
            return provider.getIntegrityHasher(algo)!!  // default algorithm will never be null
        }
    }

    /**
     * Feeds in data into this hasher.
     */
    public fun feed(data: ByteString)

    /**
     * Completes the hashing operation and returns the [IntegrityHash] over all of the data
     * provided.
     *
     * This method closes the hasher, and it cannot be used afterwards.
     */
    public fun hash(): IntegrityHash
}
