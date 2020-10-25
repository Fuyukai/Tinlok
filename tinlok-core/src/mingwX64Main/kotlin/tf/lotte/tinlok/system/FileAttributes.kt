/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_REPARSE_POINT

/**
 * Wrapper class over the raw
 */
@OptIn(ExperimentalUnsignedTypes::class)
public data class FileAttributes
public constructor(
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
    public val isDirectory: Boolean get() = (attributes.and(FILE_ATTRIBUTE_DIRECTORY)) != 0

    /** If this file is a symbolic link. */
    public val isSymlink: Boolean get() = (attributes.and(FILE_ATTRIBUTE_REPARSE_POINT)) == 0

    /** If this file is just a regular file. */
    public val isRegularFile: Boolean get() {
        return !isDirectory && !isSymlink
    }
}