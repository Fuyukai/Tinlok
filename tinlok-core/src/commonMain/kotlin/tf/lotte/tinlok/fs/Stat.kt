/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs

/**
 * Represents the result of an ``(l)stat()`` call.
 *
 * Note that some fields are system dependant; they will have corresponding Path functions for
 * their purposes instead.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public data class Stat(
    /** The owner UID of this file. */
    val ownerUID: Int,
    /** The owner GID of this file. */
    val ownerGID: Int,

    /** The size of this file, in bytes. */
    val size: Long,

    /** The device ID of this file. */
    val deviceId: ULong,

    // internal properties
    private val st_mode: UInt,
) {
    private companion object {
        const val S_IFMT = 61440U
        const val S_ISDIR = 16384U
        const val S_ISLNK = 40960U
        const val S_ISREG = 32768U
    }

    /** If this stat is for a directory. */
    public val isDirectory: Boolean
        get() = (st_mode and S_IFMT) == S_ISDIR

    /** If this stat is for a symlink. */
    public val isLink: Boolean
        get() = (st_mode and S_IFMT) == S_ISLNK

    /** If this stat is for a regular file. */
    public val isFile: Boolean
        get() = (st_mode and S_IFMT) == S_ISREG

    /**
     * The permission bits for this stat.
     */
    public val permBits: UInt
        get() = (st_mode and (0xfffu))
}
