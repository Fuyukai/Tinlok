/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs

import platform.posix.*
import tf.lotte.tinlok.ByteString
import tf.lotte.tinlok.exc.ClosedException
import tf.lotte.tinlok.fs.StandardOpenModes.*
import tf.lotte.tinlok.fs.path.LinuxPath
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.Unsafe

/**
 * Linux implementation of a synchronous filesystem file.
 */
@OptIn(Unsafe::class)
internal class LinuxSyncFile(
    override val path: LinuxPath,
    openModes: Set<FileOpenMode>,
    permission: PosixFilePermission = PosixFilePermission.DEFAULT_FILE
) : FilesystemFile {
    /** The underlying file descriptor for this file. */
    private val fd: FD

    init {

        // initial read/write mode check
        var mode = when {
            openModes.containsAll(listOf(READ, WRITE)) -> O_RDWR
            openModes.contains(READ) -> O_RDONLY
            openModes.contains(WRITE) -> O_WRONLY
            openModes.contains(APPEND) -> O_WRONLY or O_APPEND
            else -> 0
        }

        if (openModes.contains(CREATE)) {
            mode = mode or O_CREAT
        } else if (openModes.contains(CREATE_NEW)) {
            mode = mode or O_CREAT or O_EXCL
        }

        // only pass permission bit if O_CREAT is passed
        fd = if ((mode and O_CREAT) == 0) {
            Syscall.open(path.unsafeToString(), mode)
        } else {
            Syscall.open(path.unsafeToString(), mode, permission.bit)
        }
    }

    // manually handled

    // == closing == //
    override var isOpen: Boolean = true
        private set

    @OptIn(Unsafe::class)
    override fun close() {
        if (!isOpen) return
        isOpen = false
        Syscall.close(fd)
    }

    @OptIn(Unsafe::class)
    override fun cursor(): Long {
        if (!isOpen) throw ClosedException("This file is closed")
        return Syscall.lseek(fd, 0L, SEEK_CUR)
    }

    @OptIn(Unsafe::class)
    override fun seekAbsolute(position: Long) {
        if (!isOpen) throw ClosedException("This file is closed")
        Syscall.lseek(fd, position, SEEK_SET)
    }

    @OptIn(Unsafe::class)
    override fun seekRelative(position: Long) {
        if (!isOpen) throw ClosedException("This file is closed")
        Syscall.lseek(fd, position, SEEK_CUR)
    }

    // == reading == //
    @OptIn(Unsafe::class)
    override fun readUpTo(bytes: Long): ByteString? {
        if (!isOpen) throw ClosedException("This file is closed")

        val ba = ByteArray(bytes.toInt())
        val cnt = Syscall.read(fd, ba, bytes.toInt())

        // EOF
        if (cnt == 0L) return null
        // big...
        if (cnt == bytes) return ByteString.fromUncopied(ba)
        // too small :(
        return ByteString.fromUncopied(ba.copyOfRange(0, cnt.toInt()))
    }

    @OptIn(Unsafe::class)
    override fun readAll(): ByteString {
        val size = path.size() - cursor()
        if (size >= Syscall.IO_MAX) {
            throw UnsupportedOperationException("File is too big to read in one go currently")
        }

        val sizeInt = size.toInt()
        val buf = ByteArray(sizeInt)
        val cnt = Syscall.read(fd, buf, sizeInt)

        // TODO: We can avoid this mess on the sad path by reading in chunks
        return ByteString.fromUncopied(if (cnt == buf.size.toLong()) {
            buf
        } else {
            // ugh
            if (cnt > buf.size) error("What the fuck?")
            else {
                // gotta make a copy...
                // not ideal!
                buf.copyOfRange(0, cnt.toInt())
            }
        })
    }

    // == writing == //
    @OptIn(Unsafe::class)
    override fun writeAll(bs: ByteString) {
        if (!isOpen) throw ClosedException("This file is closed")

        val ba = bs.unwrap()
        Syscall.write(fd, ba, ba.size)
    }
}
