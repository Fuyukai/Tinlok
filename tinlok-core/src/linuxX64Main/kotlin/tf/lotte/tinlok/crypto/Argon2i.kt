/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("RedundantVisibilityModifier")  // not true!

package tf.lotte.tinlok.crypto

import kotlinx.cinterop.*
import tf.lotte.cc.Unsafe
import tf.lotte.cc.types.ByteString
import tf.lotte.tinlok.interop.libmonocypher.crypto_argon2i
import kotlin.random.nextUBytes

public const val HASH_SIZE: Int = 64

/**
 * Helper function for actually performing the argon2i operation.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public fun crypto_argon2(
    password: UByteArray, blocks: Int, iterations: Int,
    salt: UByteArray
): UByteArray = memScoped {
    val size = blocks * 1024

    // kinda weird but this doesn't segfault.
    val working = alloc(size, 1).reinterpret<COpaquePointerVar>()
    val output = UByteArray(HASH_SIZE)

    password.usePinned { pw ->
        salt.usePinned { s ->
            output.usePinned { h ->
                crypto_argon2i(
                    h.addressOf(0), output.size.toUInt(),
                    working.ptr,
                    blocks.toUInt(), iterations.toUInt(),
                    pw.addressOf(0), password.size.toUInt(),
                    s.addressOf(0), salt.size.toUInt()
                )
            }
        }
    }

    output
}

/**
 * Hashes a password using crypto_argon2i.
 */
@OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
public actual fun passwordHash(
    password: String, salt: UByteArray?,
    blocks: Int, iterations: Int
): Argon2iHash = memScoped {
    require(password.isNotEmpty()) { "Password cannot be empty!" }
    require(blocks >= 8) { "Must use at least 8 memory blocks!" }

    val realSalt = if (salt == null || salt.isEmpty()) {
        SecureRandom.nextUBytes(16)
    } else {
        require(salt.size >= 8) { "Salt must be at least 8 bytes" }
        salt
    }

    val encodedPw = password.encodeToByteArray().toUByteArray()
    val hashOutput = crypto_argon2(encodedPw, blocks, iterations, realSalt)

    return Argon2iHash(
        derived = ByteString.fromUncopied(hashOutput.toByteArray()),
        salt = ByteString.fromUncopied(realSalt.toByteArray()),
        blocks = blocks, iterations = iterations
    )
}
