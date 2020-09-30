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
 * Creates a new integrity hash using the blake2b algorithm.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect inline fun <R> IntegrityHasher.Companion.blake2b(
    key: UByteArray = ubyteArrayOf(),
    block: (IntegrityHasher) -> R
): R

/**
 * Compares this [ByteString] to another [ByteString] in constant time.
 */
public expect fun ByteString.constantTimeCompare(other: ByteString): Boolean
