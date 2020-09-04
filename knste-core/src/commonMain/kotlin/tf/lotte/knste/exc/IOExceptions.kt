/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.exc

import tf.lotte.knste.fs.path.PurePath
import tf.lotte.knste.util.Unsafe

/**
 * Base super class for all I/O exceptions.
 */
public open class IOException(
    message: String? = null, cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when a file already exists on a creation attempt.
 */
public open class FileAlreadyExistsException(
    public val path: String, cause: Throwable? = null
) : IOException(path, cause)

/**
 * Thrown when a file *doesn't* exist.
 */
public open class FileNotFoundException(
    public val path: String, cause: Throwable? = null
) : IOException(path, cause)

