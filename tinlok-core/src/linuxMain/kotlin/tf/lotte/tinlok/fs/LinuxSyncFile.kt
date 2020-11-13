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
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.fs.StandardOpenModes.*
import tf.lotte.tinlok.fs.path.LinuxPath
import tf.lotte.tinlok.io.Buffer
import tf.lotte.tinlok.io.checkCapacityWrite
import tf.lotte.tinlok.io.remaining
import tf.lotte.tinlok.system.*
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.ByteString
import tf.lotte.tinlok.util.ClosedException

/**
 * Linux implementation of a synchronous filesystem file.
 */
internal class LinuxSyncFile(
    override val path: LinuxPath,
    openModes: Set<FileOpenMode>,
    permission: PosixFilePermission = PosixFilePermission.DEFAULT_FILE,
) : SynchronousFile {
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

        @OptIn(Unsafe::class)
        val fd = if ((mode and O_CREAT) == 0) {
            Syscall.open(path.unsafeToString(), mode)
        } else {
            Syscall.open(path.unsafeToString(), mode, permission.bit)
        }

        this.fd = fd
    }

    override val isOpen: AtomicBoolean = AtomicBoolean(true)

    private fun checkOpen() {
        if (!isOpen) throw ClosedException("File is closed")
    }

    @OptIn(Unsafe::class)
    override fun close() {
        if (!isOpen.compareAndSet(expected = true, new = false)) return

        Syscall.close(fd)
    }

    @OptIn(Unsafe::class)
    override fun cursor(): Long {
        checkOpen()

        return Syscall.lseek(fd, 0L, SeekWhence.CURRENT)
    }

    @OptIn(Unsafe::class)
    override fun seekAbsolute(position: Long) {
        checkOpen()

        Syscall.lseek(fd, position, SeekWhence.START)
    }

    @OptIn(Unsafe::class)
    override fun seekRelative(position: Long) {
        checkOpen()

        Syscall.lseek(fd, position, SeekWhence.CURRENT)
    }

    /**
     * Reads [size] amount of bytes into the specified [buffer], returning the number of bytes
     * actually read.
     */
    @OptIn(Unsafe::class)
    override fun readInto(buffer: Buffer, size: Int): Int {
        checkOpen()
        // ensure we can actually write into a buffer of that size
        buffer.checkCapacityWrite(size)

        return if (!buffer.supportsAddress()) {
            val ba = ByteArray(size)
            val amount = Syscall.read(fd, ba, size, offset = 0)
            buffer.writeFrom(ba, ba.size, 0)
            amount.ensureNonBlock().toInt()
        } else {
            buffer.address(0) {
                Syscall.read(fd, it, size)
            }.ensureNonBlock().toInt()
        }
    }

    /**
     * Reads no more than [size] amount of bytes into [ba], starting at [offset], returning the
     * number of bytes actually written.
     */
    @OptIn(Unsafe::class)
    override fun readInto(ba: ByteArray, size: Int, offset: Int): Int {
        checkOpen()

        val result = Syscall.read(fd, ba, size, offset).ensureNonBlock()
        return result.toInt()
    }

    /**
     * Reads all of the bytes of this file.
     */
    @OptIn(Unsafe::class)
    override fun readAll(): ByteString {
        checkOpen()

        // get the correct size for symlinks
        val realPath = path.resolveFully(strict = true)
        val size = realPath.size() - cursor()
        if (size >= Syscall.IO_MAX) {
            throw UnsupportedOperationException("File is too big to read in one go currently")
        }

        val sizeInt = size.toInt()
        val buf = ByteArray(sizeInt)
        val cnt = Syscall.read(fd, buf, size = sizeInt).ensureNonBlock()

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

    /**
     * Attempts to write the entirety of the ByteArray [ba] to this object, returning the number of
     * bytes actually written before reaching EOF.
     */
    @OptIn(Unsafe::class)
    override fun writeAllFrom(ba: ByteArray): Int {
        checkOpen()

        // retry loop to ensure all is written
        var written = 0
        while (true) {
            val result = Syscall.write(fd, ba, ba.size - written, written).ensureNonBlock()
            written += result.toInt()

            if (written >= ba.size) {
                break
            }
        }

        return written
    }

    /**
     * Attempts to write the entirety of the buffer [buffer] from the cursor onwards to this object,
     * returning the number of bytes actually written before reaching EOF.
     */
    @OptIn(Unsafe::class)
    override fun writeAllFrom(buffer: Buffer): Int {
        checkOpen()

        // ensure we're not trying to write to a buffer with no space left
        if (buffer.cursor >= buffer.capacity - 1) return 0

        // slow path, need to copy it from the buffer into a bytearray
        // e.g. buffer-mapped sockets
        if (!buffer.supportsAddress()) {
            val ba = buffer.readArray((buffer.capacity - buffer.cursor).toInt())
            return writeAllFrom(ba)
        }

        // fast path, write directly using the address
        var size = (buffer.capacity - buffer.cursor).toInt()
        var total = 0
        while (true) {
            // offset zero is ALWAYS relative to the buffer cursor
            // so we always pass zero
            val result = buffer.address(0) {
                Syscall.write(fd, it, size)
            }.ensureNonBlock().toInt()

            // written up to the amount remaining, no more retries needed
            if (result >= buffer.remaining) {
                break
            }

            size -= result
            total += result
            // next read will now bee after the number of reads we did
            buffer.cursor += result.toLong()
        }

        return total
    }
}
