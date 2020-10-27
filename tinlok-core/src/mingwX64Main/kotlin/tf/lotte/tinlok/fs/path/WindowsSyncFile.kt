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
import tf.lotte.cc.Unsafe
import tf.lotte.cc.types.ByteString
import tf.lotte.tinlok.fs.FileOpenMode
import tf.lotte.tinlok.fs.FilesystemFile
import tf.lotte.tinlok.fs.StandardOpenModes.*
import tf.lotte.tinlok.fs.StandardOpenModes.CREATE_NEW
import tf.lotte.tinlok.system.SeekWhence
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.AtomicBoolean

/**
 * Implements synchronous file I/O for Windows.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class WindowsSyncFile(
    override val path: Path,
    modes: Array<out FileOpenMode>
) : FilesystemFile {
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
            else -> 0
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

    @OptIn(Unsafe::class)
    override fun close() {
        if (!isOpen.value) return

        isOpen.value = false
        Syscall.CloseHandle(handle)
    }

    @OptIn(Unsafe::class)
    override fun cursor(): Long {
        return Syscall.SetFilePointer(handle, 0, SeekWhence.CURRENT)
    }

    @OptIn(Unsafe::class)
    override fun seekAbsolute(position: Long) {
        // TODO: Reimpl so that this doesn't truncate.
        Syscall.SetFilePointer(handle, position.toInt(), SeekWhence.START)
    }

    @OptIn(Unsafe::class)
    override fun seekRelative(position: Long) {
        // TODO: Reimpl so that this doesn't truncate.
        Syscall.SetFilePointer(handle, position.toInt(), SeekWhence.CURRENT)
    }

    @OptIn(Unsafe::class)
    override fun readInto(buf: ByteArray, offset: Int, size: Int): Int {
        return Syscall.ReadFile(handle, buf, size, offset)
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

    @OptIn(Unsafe::class)
    override fun writeAllFrom(buf: ByteArray): Int {
        return Syscall.WriteFile(handle, buf)
    }
}