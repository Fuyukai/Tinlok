/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.cc.Unsafe

/**
 * A socket option that has a boolean parameter.
 */
@Unsafe
public expect class BooleanSocketOption(
    bsdOptionValue: Int,
    level: Int,
    name: String
) : BsdSocketOption<Boolean> {
    override val bsdOptionValue: Int
}

/**
 * A socket option that has an unsigned long parameter.
 */
@Unsafe
@OptIn(ExperimentalUnsignedTypes::class)
public expect class ULongSocketOption(
    bsdOptionValue: Int,
    level: Int,
    name: String
) : BsdSocketOption<ULong> {
    override val bsdOptionValue: Int
}
