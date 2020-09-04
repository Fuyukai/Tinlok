/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs

import kotlinx.cinterop.memScoped
import platform.posix.*
import tf.lotte.knste.ByteString
import tf.lotte.knste.fs.path.LinuxPath
import tf.lotte.knste.fs.StandardOpenModes.*
import tf.lotte.knste.impls.FD
import tf.lotte.knste.impls.Syscall
import tf.lotte.knste.util.Unsafe

/**
 * Linux implementation of a synchronous filesystem file.
 */
@OptIn(Unsafe::class)
internal class LinuxSyncFile(
    override val path: LinuxPath,
    openModes: Set<FileOpenMode>,
    permission: PosixFilePermission = PosixFilePermission.DEFAULT_FILE
) : FilesystemFile {
    private val fd: FD

    init {
        // initial read/write mode check
        var mode = when {
            openModes.containsAll(listOf(READ, WRITE)) -> O_RDWR
            openModes.contains(READ) -> O_RDONLY
            openModes.contains(WRITE) -> O_WRONLY
            openModes.contains(APPEND) -> O_APPEND
            else -> 0
        }

        if (openModes.contains(CREATE)) {
            mode = mode or O_CREAT
        } else if (openModes.contains(CREATE_NEW)) {
            mode = mode or O_CREAT or O_EXCL
        }

        fd = Syscall.open(path.unsafeToString(), mode, permission.bit)
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
    override val cursorPosition: Long
        get() = Syscall.lseek(fd, 0L, SEEK_CUR)

    @OptIn(Unsafe::class)
    override fun seekAbsolute(position: Long) {
        Syscall.lseek(fd, position, SEEK_SET)
    }

    @OptIn(Unsafe::class)
    override fun seekRelative(position: Long) {
        Syscall.lseek(fd, position, SEEK_CUR)
    }

    // == reading == //
    @OptIn(Unsafe::class)
    override fun readUpTo(bytes: Long): ByteString? {
        if (!isOpen) TODO("Not open")

        val ba = ByteArray(bytes.toInt())
        val cnt = Syscall.read(fd, ba, bytes.toInt())

        // EOF
        if (cnt == 0L) return null
        // big...
        if (cnt == bytes) return ByteString.fromUncopied(ba)
        // too small :(
        return ByteString.fromUncopied(ba.copyOfRange(0, cnt.toInt()))
    }

    // == writing == //
    @OptIn(Unsafe::class)
    override fun writeAll(bs: ByteString) {
        val ba = bs.unwrap()
        Syscall.write(fd, ba, ba.size)
    }
}
