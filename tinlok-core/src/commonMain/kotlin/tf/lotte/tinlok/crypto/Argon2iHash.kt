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
 * Represents a password hash that was created using the Argon2i hashing algorithm.
 */
public class Argon2iHash internal constructor(
    /** The derived final hash. */
    public val derived: ByteString,
    /** The salt used when hashing. */
    public val salt: ByteString,
    /** The number of memory blocks. */
    public val blocks: Int,
    /** The number of iterations. */
    public val iterations: Int,
) {
    /**
     * Verifies if this password hash matches a password.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public fun verify(password: String): Boolean {
        val nextHash = passwordHash(
            password, salt.unwrap().toUByteArray(), blocks, iterations
        )
        return crypto_verify64(
            derived.toUByteArray(),
            nextHash.derived.toUByteArray()
        )
    }
}
