/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package tf.lotte.knste.impls

import kotlinx.cinterop.*
import platform.posix.*
import tf.lotte.knste.exc.FileAlreadyExistsException
import tf.lotte.knste.exc.FileNotFoundException
import tf.lotte.knste.exc.IOException
import tf.lotte.knste.fs.path.PurePath
import kotlin.experimental.ExperimentalTypeInference

internal typealias FD = Int

/**
 * Namespace object for all the libc calls.
 *
 * This is preferred over regular libc calls as it throws exceptions appropriately. This object
 * is very foot-gunny, but assertions are provided for basic sanity checks.
 */
@OptIn(ExperimentalTypeInference::class)
public object Syscall {
    public const val ERROR: Int = -1
    public const val LONG_ERROR: Long = -1L

    // See: https://www.python.org/dev/peps/pep-0475/#rationale
    // Not completely the same, but similar justification.

    /**
     * Retries a function that uses C error handling semantics if EINTR is returned.
     */
    public inline fun retry(block: () -> Int): Int {
        while (true) {
            val result = block()
            if (result == ERROR && errno == EINTR) continue
            return result
        }
    }

    /**
     * Retry, but for Long.
     */
    @OverloadResolutionByLambdaReturnType  // magic!
    public inline fun retry(block: () -> Long): Long {
        while (true) {
            val result = block()
            if (result == LONG_ERROR && errno == EINTR) continue
            return result
        }
    }

    /**
     * Gets the current errno strerror().
     */
    public fun strerror(): String {
        return strerror(errno)?.toKString() ?: "Unknown error"
    }

    // == File I/O == //
    // region File I/O

    /**
     * Opens a new file descriptor for the path [path].
     */
    public fun open(path: PurePath, mode: Int, permissions: Int): FD {
        val strPath = path.unsafeToString()
        val fd = retry { platform.posix.open(strPath, mode, permissions) }
        if (fd == ERROR) {
            throw when (errno) {
                EEXIST -> FileAlreadyExistsException(path)
                ENOENT -> FileNotFoundException(path)
                EACCES -> TODO("EACCES")
                else -> IOException(strerror())
            }
        }

        return fd
    }

    /**
     * Closes a file descriptor.
     */
    public fun close(fd: FD) {
        val res = platform.posix.close(fd)
        if (res == ERROR) {
            throw IOException(strerror())
        }
    }

    // endregion

    // == Generic Linux I/O == //
    // region Linux I/O
    /**
     * Reads up to [count] bytes from file descriptor [fd] into the buffer [buf].
     */
    public fun read(fd: FD, buf: ByteArray, count: Int): Long {
        assert(count <= 0x7ffff000) { "Count is too high!" }
        assert(buf.size >= count) { "Buffer is too small!" }

        val count = buf.usePinned {
            retry { read(fd, it.addressOf(0), count.toULong()) }
        }

        if (count == LONG_ERROR) {
            // TODO: EAGAIN
            throw IOException(strerror())
        }

        return count
    }

    /**
     * Writes up to [size] bytes to the specified file descriptor, returning the number of bytes
     * actually written.
     *
     * This handles EINTR transparently, continuing a write if interrupted.
     */
    public fun write(fd: FD, from: ByteArray, size: Int): Long {
        assert(size <= 0x7ffff000) { "Count is too high!" }
        assert(from.size >= size) { "Buffer is too small!" }

        // number of bytes we have successfully written as returned from write()
        var totalWritten = 0

        // head spinny logic
        from.usePinned {
            while (true) {
                val written = write(
                    fd, it.addressOf(totalWritten),
                    (size - totalWritten).toULong()
                )

                // eintr means it didn't write anything, so we can transparently retry
                if (written == LONG_ERROR && errno != EINTR) {
                    throw IOException(strerror())
                }

                // make sure we actually write all of the bytes we want to write
                // this will never be more than INT_MAX, so we're fine
                totalWritten += written.toInt()
                if (totalWritten >= size) {
                    break
                }
            }
        }

        return totalWritten.toLong()
    }

    /**
     * Performs a seek operation on the file descriptor [fd].
     */
    public fun lseek(fd: FD, position: Long, whence: Int): Long {
        val res = platform.posix.lseek(fd, position, whence)
        if (res == LONG_ERROR) {
            throw IOException(strerror())
        }

        return res
    }

    // endregion

    /**
     * Gets statistics about a file.
     */
    public fun stat(
        alloc: NativePlacement, path: PurePath, followSymlinks: Boolean
    ): stat {
        val strPath = path.unsafeToString()
        val pathStat = alloc.alloc<stat>()

        val res =
            if (followSymlinks) stat(strPath, pathStat.ptr)
            else lstat(strPath, pathStat.ptr)

        if (res == ERROR) {
            throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                else -> IOException(strerror())
            }
        }

        return pathStat
    }
}
