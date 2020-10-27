/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import kotlinx.cinterop.Arena
import kotlinx.cinterop.alloc
import platform.windows.HANDLE
import platform.windows.WIN32_FIND_DATAW
import tf.lotte.cc.Closeable
import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.fs.DirEntry
import tf.lotte.tinlok.fs.path.Path
import kotlin.native.concurrent.ensureNeverFrozen

/**
 * An opaque object used to hold a directory scan context.
 */
@Unsafe
public class DirectoryScanContext(internal val path: Path) : Closeable {
    private val arena = Arena()
    internal val struct = arena.alloc<WIN32_FIND_DATAW>()

    internal var isOpen: Boolean = false
    internal lateinit var handle: HANDLE

    init {
        ensureNeverFrozen()
    }

    override fun close() {
        arena.clear()
        Syscall.CloseHandle(handle)
    }

    /**
     * Gets the next [DirEntry] for this context.
     */
    public fun next(): DirEntry? {
        return if (!isOpen) {
            Syscall.FindFirstFile(this)
        } else {
            Syscall.FindNextFile(this)
        }
    }
}