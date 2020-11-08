/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.util.toInt
import tf.lotte.tinlok.util.toLong
import kotlin.experimental.and
import kotlin.random.Random

/**
 * Shared object between all SecureRandom implementations.
 */
public abstract class RandomShared : Random() {
    protected abstract fun readBytesImpl(buf: ByteArray)

    override fun nextBytes(size: Int): ByteArray {
        val ba = ByteArray(size)
        readBytesImpl(ba)
        return ba
    }

    override fun nextBytes(array: ByteArray): ByteArray {
        readBytesImpl(array)
        return array
    }

    override fun nextBits(bitCount: Int): Int {
        val numBytes = (bitCount + 7) / 8
        var next = 0
        val buf = nextBytes(numBytes)

        for (i in 0 until numBytes) {
            next = (next shl 8) + (buf[i] and ((0xFF).toByte()))
        }

        return next ushr (numBytes * 8 - bitCount)
    }

    override fun nextInt(): Int {
        return nextBytes(4).toInt()
    }

    override fun nextLong(): Long {
        return nextBytes(8).toLong()
    }

}
