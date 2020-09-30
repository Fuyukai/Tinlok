/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import kotlinx.cinterop.*
import tf.lotte.tinlok.interop.libsodium.*
import tf.lotte.tinlok.types.bytestring.ByteString

/**
 * Implements an integrity hasher using crypto_generichash.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class CryptoGenerichash(
    key: UByteArray = ubyteArrayOf(), private val allocator: NativePlacement
) : IntegrityHasher {
    private var usable: Boolean = false
    private val context: crypto_generichash_state

    init {
        require(key.size.toUInt() <= crypto_generichash_KEYBYTES_MAX) {
            "Key is too big!"
        }

        if (sodium_init() == -1) {
            TODO("Failed to initialise libsodium")
        }

        context = allocator.alloc()
        key.usePinned {
            val res = crypto_generichash_init(
                context.ptr,
                key = it.addressOf(0), keylen = key.size.toULong(),
                outlen = crypto_generichash_BYTES.toULong()
            )

            if (res != 0) TODO("crypto error")
        }

        usable = true
    }

    override fun feed(data: ByteString) {
        if (!usable) error("Hash has already been consumed")

        val realData = data.unwrap().toUByteArray()
        realData.usePinned {
            val res = crypto_generichash_update(
                context.ptr, it.addressOf(0), realData.size.toULong()
            )

            if (res != 0) TODO("crypto error")
        }
    }

    override fun hash(): IntegrityHash {
        if (!usable) error("Hash has already been consumed")

        val final = UByteArray(crypto_generichash_BYTES.toInt())
        final.usePinned {
            val res = crypto_generichash_final(
                context.ptr, it.addressOf(0), final.size.toULong()
            )

            if (res != 0) TODO("crypto error")
        }
        usable = false

        val bytes = ByteString.fromUncopied(final.toByteArray())
        return IntegrityHash(bytes)
    }
}
