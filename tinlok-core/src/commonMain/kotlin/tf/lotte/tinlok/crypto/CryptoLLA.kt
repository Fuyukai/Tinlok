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
 * Verifies if two UByteArray's with a length of 64 are the same.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect fun crypto_verify64(first: UByteArray, second: UByteArray): Boolean
