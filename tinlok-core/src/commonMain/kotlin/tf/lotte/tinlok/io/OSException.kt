/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.lotte.tinlok.io

// See: https://docs.python.org/3/library/exceptions.html#OSError
/**
 * Thrown when a platform call returns an unhandleable error.
 */
public open class OSException(
    message: String? = null, cause: Throwable? = null,
) : Exception(message, cause)

// See: https://www.python.org/dev/peps/pep-3151/
// Some descriptions has been taken and reworded slightly from the Python documentation.
// See: https://docs.python.org/3/library/exceptions.html for the original texts.

/**
 * Thrown when a file already exists on a creation attempt. Corresponds to EEXIST.
 */
public open class FileAlreadyExistsException
public constructor(
    public val path: String,
    cause: Throwable?,
) : OSException(message = "File exists: $path", cause = cause) {
    public constructor(path: String) : this(path, null)
}

/**
 * Thrown when a file *doesn't* exist. Corresponds to ENOENT.
 */
public open class FileNotFoundException
constructor(
    public val path: String,
    cause: Throwable?,
) : OSException(message = "File not found: $path", cause = cause) {
    public constructor(path: String) : this(path, null)
}

/**
 * Thrown when a path is a directory and an attempt is made to treat it as a regular file.
 * Corresponds to EISDIR.
 */
public open class IsADirectoryException
constructor(
    public val path: String,
    cause: Throwable?,
) : OSException(message = "Is a directory: $path", cause = cause) {
    public constructor(path: String) : this(path, null)
}

/**
 * Thrown when a path is a directory and it is not empty.
 */
public open class DirectoryNotEmptyException
constructor(
    public val path: String,
    cause: Throwable?,
) : OSException(message = "Directory not empty: $path", cause = cause) {
    public constructor(path: String) : this(path, null)
}

/**
 * Thrown when trying to run an operation without the adequate access rights - for example
 * filesystem permissions. Corresponds to EACCES.
 */
public open class AccessDeniedException
constructor(
    cause: Throwable?,
) : OSException(message = "Access denied", cause = cause) {
    public constructor() : this(null)
}

/**
 * Thrown when trying to run an operation without the correct permission. Corresponds to EPERM.
 */
public open class PermissionDeniedException
constructor(
    cause: Throwable?,
) : OSException(message = "Permission denied", cause = cause) {
    public constructor() : this(null)
}

/**
 * Base superclass for all connection exceptions. Takes the same parameters as [OSException].
 */
public open class ConnectionException
constructor(
    message: String?, cause: Throwable?,
) : OSException(message, cause)

/**
 * Thrown when trying to write on a pipe while the other end has been closed. Corresponds to
 * EPIPE.
 */
public open class BrokenPipeException
constructor(
    cause: Throwable?,
) : ConnectionException(message = "Broken pipe", cause = cause) {
    public constructor() : this(null)
}

/**
 * Thrown when trying to write on a socket which has been shutdown for writing. Corresponds to
 * ESHUTDOWN.
 */
public open class SocketShutdownException
constructor(
    cause: Throwable?,
) : ConnectionException(message = "Cannot send after transport endpoint shutdown", cause = cause) {
    public constructor() : this(null)
}

/**
 * Thrown when a connection attempt is aborted by the remote end. Corresponds to ECONNABORTED.
 */
public open class ConnectionAbortedException
constructor(
    cause: Throwable?,
) : ConnectionException(
    message = "Connection aborted", cause = cause
) {
    public constructor() : this(null)
}

/**
 * Thrown when a connection attempt is refused by the remote end. Corresponds to ECONNREFUSED.
 */
public open class ConnectionRefusedException
constructor(cause: Throwable?) : ConnectionException(
    message = "Connection refused by peer", cause = cause
) {
    public constructor() : this(null)
}

/**
 * Thrown when a connection is reset by the remote end. Corresponds to ECONNRESET.
 */
public open class ConnectionResetException
constructor(cause: Throwable?) : ConnectionException(
    message = "Connection reset by peer", cause = cause
) {
    public constructor() : this(null)
}

/**
 * Thrown when a timeout happens on a connection. Corresponds to ETIMEDOUT.
 */
public open class TimeoutException
constructor(cause: Throwable?) : ConnectionException(
    message = "Connection timed out", cause = cause
) {
    public constructor() : this(null)
}

public open class NetworkUnreachableException
constructor(cause: Throwable?) : ConnectionException(
    message = "Network unreachable", cause = cause
) {
    public constructor() : this(null)
}
