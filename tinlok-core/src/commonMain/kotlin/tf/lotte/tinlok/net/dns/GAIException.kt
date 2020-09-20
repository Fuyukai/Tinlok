/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.dns

import tf.lotte.tinlok.exc.OSException

/**
 * Thrown when an error occurs with the getaddrinfo() system call.
 */
public class GAIException(
    errno: Int, winerror: Int = 0, message: String? = null, cause: Throwable? = null
) : OSException(errno, winerror, message, cause)
