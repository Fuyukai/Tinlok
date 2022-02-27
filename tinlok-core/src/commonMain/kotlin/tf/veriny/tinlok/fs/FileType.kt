/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.veriny.tinlok.fs

/**
 * Enumeration of all the possible file types
 */
public enum class FileType {
    /** This file is a block device special file */
    BLOCK_DEVICE,

    /** This file is a character device special file */
    CHARACTER_DEVICE,

    /** This file is in fact a directory, not a file */
    DIRECTORY,

    /** This file is a named pipe */
    FIFO,

    /** This file is a symbolic link */
    SYMLINK,

    /** This file is a Unix socket */
    UNIX_SOCKET,

    /** This file is unknown */
    UNKNOWN,

    /** This file is just a file! */
    REGULAR_FILE,

    ;

    public companion object
}
