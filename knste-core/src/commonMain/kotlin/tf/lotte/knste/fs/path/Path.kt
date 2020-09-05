/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */


package tf.lotte.knste.fs.path

import tf.lotte.knste.exc.FileNotFoundException
import tf.lotte.knste.ByteString
import tf.lotte.knste.fs.FileOpenMode
import tf.lotte.knste.fs.FilePermission
import tf.lotte.knste.fs.FilesystemFile
import tf.lotte.knste.fs.Stat
import tf.lotte.knste.toByteString
import tf.lotte.knste.util.Unsafe

/**
 * Similar to [PurePath], but can perform I/O operations.
 */
public interface Path : PurePath {
    // type changes
    public override val parent: Path
    public override fun join(other: PurePath): Path
    public override fun join(other: ByteString): Path
    public override fun join(other: String): Path = join(other.toByteString())

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
     * Checks if this [Path] is a regular file.
     */
    public fun isRegularFile(followSymlinks: Boolean = true): Boolean

    /**
     * Checks if this [Path] is a symbolic link.
     */
    public fun isLink(): Boolean

    /**
     * Gets the size of this file.
     */
    public fun size(): Long

    // See: pathlib.Path.resolve()
    /**
     * Resolves a path into an absolute path.
     *
     * If [strict] is true, the path must exist and [FileNotFoundException] will be raised if it
     * is not. If [strict] is false, the path will be resolved as far as possible.
     */
    public fun resolve(strict: Boolean = false): Path

    /**
     * Gets the owner username for this file, or null if this file doesn't have an owner (some
     * filesystems or virtual filesystems).
     */
    public fun owner(followSymlinks: Boolean = true): String?

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
    public fun listDir(): List<Path>

    /**
     * Removes this directory. It must be empty.
     */
    public fun removeDirectory()

    /**
     * Deletes this file or symbol link.
     */
    public fun unlink()

    /**
     * Opens this path for I/O operations, using the specified [modes].
     */
    @Unsafe
    public fun unsafeOpen(vararg modes: FileOpenMode): FilesystemFile
}
