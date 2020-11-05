/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.util

import tf.lotte.cc.types.ByteString
import tf.lotte.cc.types.toByteString


/**
 * Creates a new [ByteString] from the specified [i] ints. This is better than byteArrayOf as it
 * supports >128 <=255 literals.
 */
public fun baOf(vararg i: Int): ByteString {
    return intArrayOf(*i).map { it.toByte() }.toByteString()
}
