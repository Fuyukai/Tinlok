/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.lotte.tinlok.exc

import platform.posix.*
import tf.lotte.tinlok.system.Syscall

/**
 * Thrown when a file already exists on a creation attempt. Corresponds to EEXIST.
 */
public actual open class FileAlreadyExistsException
public actual constructor(
    public actual val path: String,
    cause: Throwable?
) : OSException(errno = EEXIST, message = Syscall.strerror(EEXIST), cause = cause) {
    public actual constructor(path: String) : this(path, null)
}

/**
 * Thrown when a file *doesn't* exist. Corresponds to ENOENT.
 */
public actual open class FileNotFoundException
actual constructor(
    public actual val path: String,
    cause: Throwable?
) : OSException(errno = ENOENT, message = Syscall.strerror(ENOENT), cause = null) {
    public actual constructor(path: String) : this(path, null)
}

/**
 * Thrown when a path is a directory and an attempt is made to treat it as a regular file.
 * Corresponds to EISDIR.
 */
public actual open class IsADirectoryException
actual constructor(
    public actual val path: String,
    cause: Throwable?
) : OSException(errno = EISDIR, message = Syscall.strerror(EISDIR), cause = cause) {
    public actual constructor(path: String) : this(path, null)
}

/**
 * Thrown when trying to run an operation without the adequate access rights - for example
 * filesystem permissions. Corresponds to EACCES.
 */
public actual open class AccessDeniedException
actual constructor(
    cause: Throwable?
) : OSException(errno = EACCES, message = Syscall.strerror(EACCES), cause = cause) {
    public actual constructor() : this(null)
}

/**
 * Thrown when trying to run an operation without the correct permission. Corresponds to EPERM.
 */
public actual open class PermissionDeniedException
actual constructor(
    cause: Throwable?
) : OSException(errno = EPERM, message = Syscall.strerror(EPERM), cause = cause) {
    public actual constructor() : this(null)
}

/**
 * Thrown when trying to write on a pipe while the other end has been closed. Corresponds to
 * EPIPE.
 */
public actual open class BrokenPipeException
actual constructor(
    cause: Throwable?
) : ConnectionException(errno = EPIPE, message = Syscall.strerror(EPIPE), cause = cause) {
    public actual constructor() : this(null)
}

/**
 * Thrown when trying to write on a socket which has been shutdown for writing. Corresponds to
 * ESHUTDOWN.
 */
public actual open class SocketShutdownException
actual constructor(
    cause: Throwable?
) : ConnectionException(errno = ESHUTDOWN, message = Syscall.strerror(ESHUTDOWN), cause = cause) {
    public actual constructor() : this(null)
}

/**
 * Thrown when a connection attempt is aborted by the remote end. Corresponds to ECONNABORTED.
 */
public actual class ConnectionAbortedException
actual constructor(
    cause: Throwable?
) : ConnectionException(
    errno = ECONNABORTED, message = Syscall.strerror(ECONNABORTED), cause = cause
) {
    public actual constructor() : this(null)
}

/**
 * Thrown when a connection attempt is refused by the remote end. Corresponds to ECONNREFUSED.
 */
public actual class ConnectionRefusedException
actual constructor(cause: Throwable?) : ConnectionException(
    errno = ECONNREFUSED, message = Syscall.strerror(ECONNABORTED), cause = cause
) {
    public actual constructor() : this(null)
}

/**
 * Thrown when a connection is reset by the remote end. Corresponds to ECONNRESET.
 */
public actual class ConnectionResetException
actual constructor(cause: Throwable?) : ConnectionException(
    errno = ECONNRESET, message = Syscall.strerror(ECONNRESET), cause = cause
) {
    public actual constructor() : this(null)
}
