/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import platform.windows.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.fs.FileOpenMode
import tf.lotte.tinlok.fs.StandardOpenModes.*
import tf.lotte.tinlok.fs.StandardOpenModes.CREATE_NEW
import tf.lotte.tinlok.fs.SynchronousFile
import tf.lotte.tinlok.io.Buffer
import tf.lotte.tinlok.io.checkCapacityWrite
import tf.lotte.tinlok.io.remaining
import tf.lotte.tinlok.system.SeekWhence
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.ByteString
import tf.lotte.tinlok.util.ClosedException

/**
 * Implements synchronous file I/O for Windows.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class WindowsSyncFile(
    override val path: Path,
    modes: Array<out FileOpenMode>,
) : SynchronousFile {
    private val handle: HANDLE

    init {
        @Suppress("NAME_SHADOWING")
        val modes = modes.toSet()

        val accessFlag = when {
            modes.containsAll(listOf(READ, WRITE)) -> (GENERIC_READ.toInt().or(GENERIC_WRITE))
            modes.contains(READ) -> GENERIC_READ.toInt()
            modes.contains(WRITE) -> GENERIC_WRITE
            modes.contains(APPEND) -> GENERIC_WRITE
            else -> 0
        }

        val creationFlag = when {
            modes.contains(CREATE) -> OPEN_ALWAYS
            modes.contains(CREATE_NEW) -> platform.windows.CREATE_NEW
            else -> OPEN_EXISTING
        }

        @OptIn(Unsafe::class)
        handle = Syscall.CreateFile(path.unsafeToString(), accessFlag, creationFlag, 0)
        // seek to end
        if (modes.contains(APPEND)) {
            @OptIn(Unsafe::class)
            Syscall.SetFilePointer(handle, 0, SeekWhence.END)
        }
    }

    override val isOpen: AtomicBoolean = AtomicBoolean(true)

    private fun checkOpen() {
        if (!isOpen) throw ClosedException("File is closed")
    }

    @OptIn(Unsafe::class)
    override fun close() {
        if (!isOpen.compareAndSet(expected = true, new = false)) return

        Syscall.CloseHandle(handle)
    }

    @OptIn(Unsafe::class)
    override fun cursor(): Long {
        checkOpen()

        return Syscall.SetFilePointer(handle, 0, SeekWhence.CURRENT)
    }

    @OptIn(Unsafe::class)
    override fun seekAbsolute(position: Long) {
        checkOpen()

        // TODO: Reimpl so that this doesn't truncate.
        Syscall.SetFilePointer(handle, position.toInt(), SeekWhence.START)
    }

    @OptIn(Unsafe::class)
    override fun seekRelative(position: Long) {
        checkOpen()

        // TODO: Reimpl so that this doesn't truncate.
        Syscall.SetFilePointer(handle, position.toInt(), SeekWhence.CURRENT)
    }


    /**
     * Reads [size] amount of bytes into the specified [buffer], returning the number of bytes
     * actually read.
     */
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
            val amount = Syscall.ReadFile(handle, ba, size, offset = 0)
            buffer.writeFrom(ba, ba.size, 0)
            amount
        } else {
            buffer.address(0) {
                Syscall.ReadFile(handle, it, size)
            }
        }
    }

    /**
     * Reads no more than [size] amount of bytes into [ba], starting at [offset], returning the
     * number of bytes actually written.
     */
    @OptIn(Unsafe::class)
    override fun readInto(ba: ByteArray, size: Int, offset: Int): Int {
        return Syscall.ReadFile(handle, ba, size, offset)
    }


    @OptIn(Unsafe::class)
    override fun readAll(): ByteString {
        val size = Syscall.GetFileSize(handle)
        if (size > Int.MAX_VALUE) throw NotImplementedError("File size too big for one buffer")

        val intSize = size.toInt()
        val buffer = ByteArray(intSize)
        val read = readInto(buffer)
        return if (read < intSize) {
            val copy = buffer.copyOfRange(0, read)
            ByteString.fromUncopied(copy)
        } else {
            ByteString.fromUncopied(buffer)
        }
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
            val result = Syscall.WriteFile(handle, ba, ba.size - written, written)
            written += result

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

        // Copied from LinuxSyncFile. Maybe in the future I'll fix this to use the same path for
        // all platforms.

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
                Syscall.WriteFile(handle, it, size)
            }

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
