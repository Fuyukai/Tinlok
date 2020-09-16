/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.exc

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

/**
 * Thrown when a path is a directory and an attempt is made to treat it as a regular file.
 */
public open class IsADirectoryException(
    public val path: String, cause: Throwable? = null,
) : IOException(path, cause)

/**
 * Base super class for all socket exceptions.
 */
public open class SocketException(
    message: String? = null, cause: Throwable? = null
) : IOException(message, cause)
