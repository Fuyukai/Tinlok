/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.exc

import tf.lotte.cc.exc.OSException

/**
 * Thrown when an exception happens that is not otherwise turned into a
 */
public class WindowsException
public constructor(
    public val winerror: Int, message: String, cause: Throwable? = null,
) : OSException(message, cause)
