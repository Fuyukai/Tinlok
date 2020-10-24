/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import platform.posix.O_RDONLY
import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.toInt
import tf.lotte.tinlok.util.toLong
import kotlin.experimental.and
import kotlin.random.Random

public actual object SecureRandom : Random() {
    // it's ok for this to leak, as we will never close it
    @OptIn(Unsafe::class)
    private val fd = Syscall.open("/dev/urandom", O_RDONLY)

    @OptIn(Unsafe::class)
    override fun nextBytes(size: Int): ByteArray {
        val buf = ByteArray(size)
        val read = Syscall.read(fd, buf, size)
        check(read == size.toLong()) { "/dev/urandom returned $read bytes instead of $size" }
        return buf
    }

    @OptIn(Unsafe::class)
    override fun nextBytes(array: ByteArray): ByteArray {
        val read = Syscall.read(fd, array, array.size)
        check(read == array.size.toLong()) {
            "/dev/urandom returned $read bytes instead of ${array.size}"
        }
        return array
    }

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        // TODO: Use a read() method with an offset.
        val buf = nextBytes(toIndex - fromIndex)
        buf.copyInto(array, destinationOffset = fromIndex)
        return array
    }

    // copied from java.lang.SecureRandom
    // except with brackets to prevent a CVE
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
