/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.exc

// See: https://docs.python.org/3/library/exceptions.html#OSError
/**
 * Thrown when a platform call returns an unhandleable error.
 */
public open class OSException(
    /** The POSIX error number. On Windows, this is an approximation. */
    public val errno: Int,
    /** The windows error number. On Linux, this is always 0. */
    public val winerror: Int = 0,

    message: String? = null, cause: Throwable? = null
) : Exception(
    if (winerror != 0) "[WinError $winerror] $message" else "[errno $errno] $message",
    cause
)

// See: https://www.python.org/dev/peps/pep-3151/
// Some descriptions has been taken and reworded slightly from the Python documentation.
// See: https://docs.python.org/3/library/exceptions.html for the original texts.

/**
 * Thrown when a file already exists on a creation attempt. Corresponds to EEXIST.
 */
public expect open class FileAlreadyExistsException(
    path: String, cause: Throwable?
) : OSException {
    public constructor(path: String)

    public val path: String
}

/**
 * Thrown when a file *doesn't* exist. Corresponds to ENOENT.
 */
public expect open class FileNotFoundException(
    path: String, cause: Throwable?
) : OSException {
    public constructor(path: String)

    public val path: String
}

/**
 * Thrown when a path is a directory and an attempt is made to treat it as a regular file.
 * Corresponds to EISDIR.
 */
public expect open class IsADirectoryException(
    path: String, cause: Throwable?
) : OSException {
    public constructor(path: String)

    public val path: String
}

/**
 * Thrown when trying to run an operation without the adequate access rights - for example
 * filesystem permissions. Corresponds to EACCES.
 */
public expect open class AccessDeniedException(cause: Throwable?) : OSException {
    public constructor()
}

/**
 * Thrown when trying to run an operation without the correct permission. Corresponds to EPERM.
 */
public expect open class PermissionDeniedException(cause: Throwable?) : OSException {
    public constructor()
}


/**
 * Base superclass for all connection exceptions. Takes the same parameters as [OSException].
 */
public open class ConnectionException(
    errno: Int, winerror: Int = 0,
    message: String? = null, cause: Throwable? = null
) : OSException(errno, winerror, message, cause)

/**
 * Thrown when trying to write on a pipe while the other end has been closed, or trying to write on
 * a socket which has been shutdown for writing. Corresponds to EPIPE and ESHUTDOWN.
 */
public expect class BrokenPipeException(cause: Throwable?) : ConnectionException {
    public constructor()
}

/**
 * Thrown when trying to write on a socket which has been shutdown for writing. Corresponds to
 * ESHUTDOWN.
 */
public expect class SocketShutdownException(cause: Throwable?) : ConnectionException {
    public constructor()
}

/**
 * Thrown when a connection attempt is aborted by the remote end. Corresponds to ECONNABORTED.
 */
public expect class ConnectionAbortedException(cause: Throwable?) : ConnectionException {
    public constructor()
}

/**
 * Thrown when a connection attempt is refused by the remote end. Corresponds to ECONNREFUSED.
 */
public expect class ConnectionRefusedException(cause: Throwable?) : ConnectionException {
    public constructor()
}

/**
 * Thrown when a connection is reset by the remote end. Corresponds to ECONNRESET.
 */
public expect class ConnectionResetException(cause: Throwable?) : ConnectionException {
    public constructor()
}

/**
 * Thrown when a timeout happens on a connection. Corresponds to ETIMEDOUT.
 */
public expect class TimeoutException(cause: Throwable?) : ConnectionException {
    public constructor()
}
