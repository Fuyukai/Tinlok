/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs

import tf.lotte.tinlok.fs.path.Path

// See: https://docs.python.org/3/library/os.html#os.DirEntry

/**
 * Represents a single entry in a directory.
 *
 * This caches the filetype returned from readdir()/equiv and only calls stat again if the
 * filetype is UNKNOWN (returned on some filesystems).
 */
public data class DirEntry(
    /** The path to the file. */
    public val path: Path,
    /** The file type of the file. This may be [FileType.UNKNOWN]. */
    public val type: FileType,
) {
    /** Returns true if this entry represents a directory. */
    public fun isDirectory(followSymlinks: Boolean): Boolean {
        if (type == FileType.UNKNOWN) {
            return path.isDirectory(followSymlinks)
        }
        return type == FileType.DIRECTORY
    }

    /** Returns true if this entry represents a regular file. */
    public fun isRegularFile(followSymlinks: Boolean = true): Boolean {
        if (type == FileType.UNKNOWN) {
            return path.isRegularFile()
        }
        return type == FileType.REGULAR_FILE
    }

    /** Returns true if this entry represents a symlink. */
    public fun isSymlink(): Boolean {
        return path.isLink()
    }
}
