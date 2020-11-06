/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.net

/**
 * Thrown when an IPv6 address fails to parse.
 */
public class IPv6ParseException(
    message: String,
    address: String,
    cause: Throwable? = null,
) : Exception("$message when parsing addressing $address", cause)