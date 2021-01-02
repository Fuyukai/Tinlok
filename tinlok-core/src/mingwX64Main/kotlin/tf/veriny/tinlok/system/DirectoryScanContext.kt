/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.system

import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import platform.windows.HANDLE
import platform.windows.WIN32_FIND_DATAW
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.fs.DirEntry
import tf.veriny.tinlok.fs.path.Path
import tf.veriny.tinlok.util.Closeable
import tf.veriny.tinlok.util.ClosedException
import kotlin.native.concurrent.ensureNeverFrozen

/**
 * An opaque object used to hold a directory scan context.
 */
@Unsafe
public class DirectoryScanContext(internal val path: Path) : Closeable {
    private val arena = Arena()
    internal val struct = arena.alloc<WIN32_FIND_DATAW>()

    internal var isOpen: Boolean = false
    internal var isClosed: Boolean = false
    internal var handle: HANDLE? = null

    init {
        ensureNeverFrozen()
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        arena.clear()

        if (handle != null) {
            Syscall.FindClose(this)
        }
    }

    /**
     * Gets the next [DirEntry] for this context.
     */
    public fun next(): DirEntry? {
        if (isClosed) throw ClosedException("This context is closed")

        return if (!isOpen) {
            Syscall.FindFirstFile(this)
        } else {
            Syscall.FindNextFile(this)
        }
    }
}
