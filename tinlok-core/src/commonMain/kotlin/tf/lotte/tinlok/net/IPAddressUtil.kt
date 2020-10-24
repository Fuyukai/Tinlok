/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.cc.Unsafe

/**
 * Parses an IPv6 address from a String to a [ByteArray] of its byte components.
 */
@Unsafe
internal expect fun parseIPv6(string: String): ByteArray

/**
 * Converts an IPv6 address to a string from its internal representation.
 */
@Unsafe
internal expect fun IPv6toString(contents: ByteArray): String
