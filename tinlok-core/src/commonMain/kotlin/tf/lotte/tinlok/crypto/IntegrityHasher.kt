/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.types.bytestring.ByteString

/**
 * A hasher that takes in data and produces an integrity hash over all of the consumed data.
 */
public interface IntegrityHasher {
    public companion object;

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
