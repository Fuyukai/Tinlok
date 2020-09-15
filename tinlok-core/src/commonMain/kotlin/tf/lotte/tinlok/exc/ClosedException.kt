/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.exc

/**
 * Thrown when something is attempted on a closed resource.
 */
public class ClosedException(
    message: String? = null, cause: Throwable? = null
) : Exception(message, cause)
