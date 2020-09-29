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
import kotlinx.cinterop.usePinned
import tf.lotte.tinlok.interop.libsodium.sodium_compare
import tf.lotte.tinlok.interop.libsodium.sodium_init
import tf.lotte.tinlok.interop.libsodium.sodium_memcmp
import tf.lotte.tinlok.util.Unsafe

/**
 * Provides cryptography actions using libsodium.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public actual class LibsodiumProvider : CryptographyProvider {
    private companion object {
        @Unsafe
        private val HASH_ALGORITHMS: Map<String, () -> IntegrityHasher> = mapOf(
            "blake2b" to { CryptoGenerichash() }
        )
    }

    init {
        // TODO: Warn users about /dev/random blocking early on.
        sodium_init()
    }

    override val defaultIntegrityHashAlgorithm: String
        get() = "blake2b"

    @Unsafe
    override fun getIntegrityHasher(algorithm: String): IntegrityHasher? {
        return HASH_ALGORITHMS[algorithm]?.invoke()
    }

    /**
     * Securely compares two [ByteArray] objects using libsodium's memory comparer.
     */
    override fun secureCompare(first: UByteArray, second: UByteArray): Boolean {
        require(first.size == second.size) {
            "The two arrays passed to secureCompare must be the same size!"
        }

        first.usePinned { p1 ->
            second.usePinned { p2 ->
                val res = sodium_memcmp(p1.addressOf(0), p2.addressOf(0), first.size.toULong())
                return res == 0
            }
        }
    }
}
