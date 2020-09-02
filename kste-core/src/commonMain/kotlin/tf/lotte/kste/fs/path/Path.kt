/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */


package tf.lotte.kste.fs.path

import tf.lotte.kste.ByteString
import tf.lotte.kste.fs.FilePermission
import tf.lotte.kste.fs.Stat

/**
 * Similar to [PurePath], but can perform I/O operations.
 */
public interface Path : PurePath {
    // type changes
    public override val parent: Path
    public override fun join(other: PurePath): Path
    public override fun join(other: ByteString): Path

    // == query operators == //
    /**
     * Checks to see if this [Path] exists.
     */
    public fun exists(): Boolean

    /**
     * Checks if this [Path] is a directory.
     */
    public fun isDirectory(followSymlinks: Boolean = true): Boolean

    /**
     * Checks if this [Path] is a file.
     */
    public fun isFile(followSymlinks: Boolean = true): Boolean

    /**
     * Checks if this [Path] is a symbolic link.
     */
    public fun isLink(): Boolean

    /**
     * Gets the ``stat`` output for this Path, or null if it doesn't exist.
     */
    public fun stat(followSymlinks: Boolean): Stat?

    // == modification operators == //
    /**
     * Creates this [Path] as a directory. If [parents] is true, creates all the parent
     * directories too. If [existOk] is true, then an existing directory will not cause an error.
     * The directory will be created with the [permissions] file permissions.
     */
    public fun createDirectory(
        parents: Boolean = false,
        existOk: Boolean = false,
        vararg permissions: FilePermission
    )

    /**
     * Lists the files in this directory.
     */
    public fun listFiles(): List<Path>

    /**
     * Removes this directory. It must be empty.
     */
    public fun removeDirectory()

    /**
     * Deletes this file or symbol link.
     */
    public fun unlink()
}
