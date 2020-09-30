/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import tf.lotte.tinlok.interop.libsodium.sodium_compare
import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.util.Unsafe

/**
 * Passes a new Blake2b hasher to the specified block.
 */
@OptIn(Unsafe::class, ExperimentalUnsignedTypes::class)
public actual inline fun <R> IntegrityHasher.Companion.blake2b(
    key: UByteArray,
    block: (IntegrityHasher) -> R
): R =
    memScoped {
        val hasher = CryptoGenerichash(key, allocator = this)
        return block(hasher)
    }

/**
 * Uses libsodium's constant time comparison function.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public actual fun ByteString.constantTimeCompare(other: ByteString): Boolean {
    require(size == other.size) { "Cannot compare two objects of different sizes!" }

    val first = unwrap().toUByteArray()
    val second = other.unwrap().toUByteArray()
    first.usePinned { i1 ->
        second.usePinned { i2 ->
            val res = sodium_compare(i1.addressOf(0), i2.addressOf(0), this.size.toULong())
            return res == 0
        }
    }
}
