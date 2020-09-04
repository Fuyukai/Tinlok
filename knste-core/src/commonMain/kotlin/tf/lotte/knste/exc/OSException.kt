/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.exc

// See: https://docs.python.org/3/library/exceptions.html#OSError
/**
 * Thrown when a system function returns an errno that is not otherwise turned into a different
 * exception.
 */
public class OSException(
    /** The POSIX error number. On Windows, this is an approximation. */
    public val errno: Int,
    /** The windows error number. On Linux, this is always 0. */
    public val winerror: Int = 0,

    message: String? = null, cause: Throwable? = null
) : Exception(
    if (winerror != 0) "[WinError $winerror] $message" else "[errno $errno] $message",
    cause
)
