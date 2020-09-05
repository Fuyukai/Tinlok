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
import tf.lotte.knste.ByteString
import tf.lotte.knste.exc.FileAlreadyExistsException
import tf.lotte.knste.exc.FileNotFoundException
import tf.lotte.knste.exc.IOException
import tf.lotte.knste.exc.OSException
import tf.lotte.knste.readZeroTerminated
import tf.lotte.knste.util.Unsafe
import kotlin.experimental.ExperimentalTypeInference

internal typealias FD = Int

// TODO: Probably want to make some of these enums.
// TODO: Some EINVAL probably can be IllegalArgumentException, rather than IOException.

/**
 * Namespace object for all the libc calls.
 *
 * This is preferred over regular libc calls as it throws exceptions appropriately. This object
 * is very foot-gunny, but assertions are provided for basic sanity checks.
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalUnsignedTypes::class)
public object Syscall {
    public const val ERROR: Int = -1
    public const val LONG_ERROR: Long = -1L

    // See: https://www.python.org/dev/peps/pep-0475/#rationale
    // Not completely the same, but similar justification.

    /**
     * Retries a function that uses C error handling semantics if EINTR is returned.
     */
    @Unsafe
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
    @Unsafe
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
    @Unsafe
    public fun strerror(): String {
        return strerror(errno)?.toKString() ?: "Unknown error"
    }

    // == File opening/closing == //
    // region File opening/closing

    @Unsafe
    private fun openShared(path: String, fd: Int): Int {
        if (fd == ERROR) {
            throw when (errno) {
                EEXIST -> FileAlreadyExistsException(path)
                ENOENT -> FileNotFoundException(path)
                EACCES -> TODO("EACCES")
                else -> OSException(errno, message = strerror())
            }
        }

        return fd
    }

    @Unsafe
    public fun open(path: String, mode: Int): FD {
        val fd = retry { platform.posix.open(path, mode) }
        return openShared(path, fd)
    }

    /**
     * Opens a new file descriptor for the path [path].
     */
    @Unsafe
    public fun open(path: String, mode: Int, permissions: Int): FD {
        val fd = retry { platform.posix.open(path, mode, permissions) }
        return openShared(path, fd)
    }

    /**
     * Closes a file descriptor.
     */
    @Unsafe
    public fun close(fd: FD) {
        val res = platform.posix.close(fd)
        if (res == ERROR) {
            throw OSException(errno, message = strerror())
        }
    }

    // endregion

    // == Generic Linux I/O == //
    // region Linux I/O
    /** The maximum size for most I/O functions. */
    public const val IO_MAX: Int = 0x7ffff000

    /**
     * Reads up to [count] bytes from file descriptor [fd] into the buffer [buf].
     */
    @Unsafe
    public fun read(fd: FD, buf: ByteArray, count: Int): Long {
        assert(count <= IO_MAX) { "Count is too high!" }
        assert(buf.size >= count) { "Buffer is too small!" }

        val readCount = buf.usePinned {
            retry { read(fd, it.addressOf(0), count.toULong()) }
        }

        if (readCount == LONG_ERROR) {
            // TODO: EAGAIN
            throw when (errno) {
                EIO -> IOException(strerror())
                else -> OSException(errno, message = strerror())
            }
        }

        return readCount
    }

    /**
     * Writes up to [size] bytes to the specified file descriptor, returning the number of bytes
     * actually written.
     *
     * This handles EINTR transparently, continuing a write if interrupted.
     */
    @Unsafe
    public fun write(fd: FD, from: ByteArray, size: Int): Long {
        assert(size <= IO_MAX) { "Count is too high!" }
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
                    throw OSException(errno, message = strerror())
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
    @Unsafe
    public fun lseek(fd: FD, position: Long, whence: Int): Long {
        val res = platform.posix.lseek(fd, position, whence)
        if (res == LONG_ERROR) {
            throw OSException(errno, message = strerror())
        }

        return res
    }

    // endregion

    // == File Polling == //
    // region File Polling
    /**
     * Gets statistics about a file.
     */
    @Unsafe
    public fun stat(
        alloc: NativePlacement, path: String, followSymlinks: Boolean
    ): stat {
        val pathStat = alloc.alloc<stat>()

        val res =
            if (followSymlinks) stat(path, pathStat.ptr)
            else lstat(path, pathStat.ptr)

        if (res == ERROR) {
            throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                else -> OSException(errno, message = strerror())
            }
        }

        return pathStat
    }

    /**
     * Gets access information about a file.
     */
    @Unsafe
    public fun access(path: String, mode: Int): Boolean {
        val result = platform.posix.access(path, mode)
        if (result == ERROR) {
            if (errno == EACCES) return false
            if (errno == ENOENT && mode == F_OK) return false
            else throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                EROFS -> IOException("Filesystem is read-only")  // TODO: Dedicated error?
                else -> OSException(errno, message = strerror())
            }
        }

        return true
    }

    /**
     * Opens a directory for file listing.
     */
    @Suppress("FoldInitializerAndIfToElvis")
    @Unsafe
    public fun opendir(path: String): CPointer<DIR> {
        val dirfd = platform.posix.opendir(path)
        if (dirfd == null) {
            throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                else -> OSException(errno, message = strerror())
            }
        }

        return dirfd
    }

    /**
     * Reads a new entry from an opened directory. Returns null on the last entry.
     */
    @Unsafe
    public fun readdir(dirfd: CValuesRef<DIR>): CPointer<dirent>? {
        return platform.posix.readdir(dirfd)
    }

    /**
     * Closes an opened directory.
     */
    @Unsafe
    public fun closedir(dirfd: CValuesRef<DIR>) {
        val res = platform.posix.closedir(dirfd)
        if (res == ERROR) {
            throw OSException(errno, message = strerror())
        }
    }

    // endregion

    // == Filesystem access == //
    /**
     * Creates a new filesystem directory.
     */
    @Unsafe
    public fun mkdir(path: String, mode: UInt, existOk: Boolean) {
        val result = mkdir(path, mode)
        if (result == ERROR) {
            if (errno == EEXIST && existOk) return
            else throw when (errno) {
                EEXIST -> FileAlreadyExistsException(path)
                ENOENT -> FileNotFoundException(path)
                else -> OSException(errno, message = strerror())
            }
        }
    }

    /**
     * Removes a filesystem directory.
     */
    @Unsafe
    public fun rmdir(path: String) {
        val result = platform.posix.rmdir(path)
        if (result == ERROR) {
            throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                else -> OSException(errno, message = strerror())
            }
        }
    }

    /**
     * Unlinks a symbolic file or deletes a file.
     */
    @Unsafe
    public fun unlink(path: String) {
        val result = platform.posix.unlink(path)
        if (result == ERROR) {
            throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                else -> OSException(errno, message = strerror())
            }
        }
    }

    /**
     * Fully resolves a path into an absolute path.
     */
    @Suppress("FoldInitializerAndIfToElvis")
    @Unsafe
    public fun realpath(alloc: NativePlacement, path: String): ByteString {
        val buffer = alloc.allocArray<ByteVar>(PATH_MAX)
        val res = realpath(path, buffer)
        if (res == null) {
            throw when (errno) {
                ENOENT -> FileNotFoundException(path)
                else -> OSException(errno, message = strerror())
            }
        }

        val ba = res.readZeroTerminated(PATH_MAX)
        return ByteString.fromRawHolder(ByteStringHolder.fromByteArrayUncopied(ba))
    }

    // == Misc == //
    // region Misc

    /**
     * Gets a [passwd] entry for the specified uid.
     */
    @Unsafe
    public fun getpwuid_r(alloc: NativePlacement, uid: UInt): passwd? {
        val passwd = alloc.alloc<passwd>()
        val starResult = alloc.allocPointerTo<passwd>()

        var bufSize = sysconf(_SC_GETPW_R_SIZE_MAX)
        if (bufSize == -1L) bufSize = 16384
        val buffer = alloc.allocArray<ByteVar>(bufSize)

        val res = getpwuid_r(uid, passwd.ptr, buffer, bufSize.toULong(), starResult.ptr)
        if (starResult.value == null) {
            throw OSException(errno, message = Syscall.strerror())
        }

        return passwd
    }

    // endregion
}
