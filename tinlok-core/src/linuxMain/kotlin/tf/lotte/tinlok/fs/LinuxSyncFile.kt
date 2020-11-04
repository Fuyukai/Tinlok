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
import tf.lotte.cc.Unsafe
import tf.lotte.cc.exc.ClosedException
import tf.lotte.cc.types.ByteString
import tf.lotte.tinlok.fs.StandardOpenModes.*
import tf.lotte.tinlok.fs.path.LinuxPath
import tf.lotte.tinlok.io.FdWrapper
import tf.lotte.tinlok.system.SeekWhence
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.system.ensureNonBlock
import tf.lotte.tinlok.util.AtomicBoolean

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
    private val wrapper: FdWrapper

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
        val fd = if ((mode and O_CREAT) == 0) {
            Syscall.open(path.unsafeToString(), mode)
        } else {
            Syscall.open(path.unsafeToString(), mode, permission.bit)
        }

        wrapper = FdWrapper(fd)
    }

    override val isOpen: AtomicBoolean
        get() = wrapper.isOpen

    @OptIn(Unsafe::class)
    override fun close() {
        wrapper.close()
    }

    @OptIn(Unsafe::class)
    override fun cursor(): Long {
        if (!isOpen) throw ClosedException("This file is closed")

        return Syscall.lseek(wrapper.fd, 0L, SeekWhence.CURRENT)
    }

    @OptIn(Unsafe::class)
    override fun seekAbsolute(position: Long) {
        if (!isOpen) throw ClosedException("This file is closed")

        Syscall.lseek(wrapper.fd, position, SeekWhence.START)
    }

    @OptIn(Unsafe::class)
    override fun seekRelative(position: Long) {
        if (!isOpen) throw ClosedException("This file is closed")

        Syscall.lseek(wrapper.fd, position, SeekWhence.CURRENT)
    }

    override fun readInto(buf: ByteArray, size: Int, offset: Int): Int {
        val result = wrapper.read(buf, size, offset).ensureNonBlock()
        return result.toInt()
    }

    @OptIn(Unsafe::class)
    override fun readAll(): ByteString {

        // get the correct size for symlinks
        val realPath = path.resolveFully(strict = true)
        val size = realPath.size() - cursor()
        if (size >= Syscall.IO_MAX) {
            throw UnsupportedOperationException("File is too big to read in one go currently")
        }

        val sizeInt = size.toInt()
        val buf = ByteArray(sizeInt)
        val cnt = wrapper.read(buf, size = sizeInt).ensureNonBlock()

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

    override fun writeAllFrom(buf: ByteArray): Int {
        return wrapper.write(buf, buf.size, 0).ensureNonBlock().toInt()
    }
}
