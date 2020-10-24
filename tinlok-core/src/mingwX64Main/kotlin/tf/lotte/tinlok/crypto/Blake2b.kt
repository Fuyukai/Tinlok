/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.cc.Closeable
import tf.lotte.tinlok.types.bytestring.ByteString

/**
 * A class that takes in data and will eventually produce a hash using the Blake2b algorithm.
 */
public actual class Blake2b internal actual constructor(key: UByteArray) : Closeable {
    public actual companion object;

    /**
     * Feeds some data into this object.
     */
    public actual fun feed(data: ByteString) {
    }

    /**
     * Performs the Blake2b hashing algorithm over all previously consumed data and returns a
     * final hash.
     */
    public actual fun hash(): Blake2bHash {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}