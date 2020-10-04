/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

/**
 * Hashes the input password [password], using the salt [salt], using the Argon2i algorithm. If no
 * salt is provided, a random one will be generated which will be enclosed in the resulting
 * [Argon2iHash] structure. The resulting hash will be 64 bytes long.
 *
 * The [blocks] and [iterations] parameters tweak the Argon2i function parameters. Higher is
 * generally better. A reasonable default of 100_000 blocks (100MiB) and 3 iterations has been
 * set as recommended by Monocypher, but you can increase the blocks count to fit your machine
 * better.
 */
public expect fun passwordHash(
    password: String, salt: UByteArray? = null,
    blocks: Int = 100_000, iterations: Int = 3
): Argon2iHash
