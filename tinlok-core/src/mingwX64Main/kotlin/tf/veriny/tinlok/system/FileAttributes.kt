/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.system

import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.util.flagged

/**
 * Wrapper class over the raw
 */
@OptIn(ExperimentalUnsignedTypes::class)
public data class FileAttributes
public constructor(
    private val path: String,
    /** The raw attributes of this file. */
    public val attributes: Int,
    /** The size of this file. */
    public val size: ULong,

    /** The creation time, represented as a Windows time. */
    public val creationTime: ULong,
    /** The modification time, represented as a Windows time. */
    public val modificationTime: ULong,
    /** The access time, represented as a Windows time. */
    public val accessTime: ULong,
) {
    /** If this file is a directory. */
    public val isDirectory: Boolean get() = flagged(attributes, FILE_ATTRIBUTE_DIRECTORY)

    /** If this file is a symbolic link. */
    @OptIn(Unsafe::class)
    public fun isSymlink(): Boolean {
        // all symlinks are reparse points
        if (!flagged(attributes, FILE_ATTRIBUTE_REPARSE_POINT)) return false

        // but not all reparse points are symlinks, so we have to actually check
        return Syscall.__is_symlink(path)
    }

    /** If this file is just a regular file. */
    public val isRegularFile: Boolean
        get() {
            return !isDirectory
        }
}
