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
import tf.lotte.tinlok.util.Unsafe

/**
 * Implements an integrity hasher using crypto_generichash.
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Unsafe
public class CryptoGenerichash(key: UByteArray = ubyteArrayOf()) : IntegrityHasher {
    private val arena = Arena()
    private var usable: Boolean = false
    private val context: crypto_generichash_state

    init {
        require(key.size.toUInt() <= crypto_generichash_KEYBYTES_MAX) {
            "Key is too big!"
        }

        context = arena.alloc()
        key.usePinned {
            crypto_generichash_init(
                context.ptr,
                key = it.addressOf(0), keylen = key.size.toULong(),
                outlen = crypto_generichash_BYTES.toULong()
            )
        }
    }

    override fun feed(data: ByteString) {
        if (!usable) error("Hash has already been consumed")

        val realData = data.unwrap().toUByteArray()
        realData.usePinned {
            crypto_generichash_update(context.ptr, it.addressOf(0), realData.size.toULong())
        }
    }

    override fun hash(): IntegrityHash {
        if (!usable) error("Hash has already been consumed")

        val final = UByteArray(crypto_generichash_BYTES.toInt())
        final.usePinned {
            crypto_generichash_final(context.ptr, it.addressOf(0), final.size.toULong())
        }
        val bytes = ByteString.fromUncopied(final.toByteArray())

        usable = false
        arena.clear()

        return IntegrityHash(bytes)
    }

    override fun close() {
        usable = false
        arena.clear()
    }
}
