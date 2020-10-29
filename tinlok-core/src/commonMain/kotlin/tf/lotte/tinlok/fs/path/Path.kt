/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */


package tf.lotte.tinlok.fs.path

import tf.lotte.cc.Unsafe
import tf.lotte.cc.exc.FileNotFoundException
import tf.lotte.cc.types.ByteString
import tf.lotte.tinlok.fs.*

/**
 * Similar to [PurePath], but can perform I/O operations.
 */
public interface Path : PurePath {
    public companion object {
        /**
         * Creates a new platform Path using the specified [PlatformPurePath].
         */
        public fun of(path: PlatformPurePath): Path {
            return PlatformPaths.path(path)
        }

        /**
         * Creates a new platform [Path].
         */
        public fun of(path: ByteString): Path {
            return PlatformPaths.path(path)
        }

        /**
         * Creates a new platform [Path] corresponding to the Current Working Directory.
         */
        public fun cwd(): Path {
            return PlatformPaths.cwd()
        }

        /**
         * Creates a new platform [Path] corresponding to the current user's HOME directory.
         */
        public fun home(): Path {
            return PlatformPaths.home()
        }
    }

    // type changes
    public override val parent: Path
    public override fun resolveChild(other: PurePath): Path
    public override fun withName(name: ByteString): Path
    public override fun reparent(from: PurePath, to: PurePath): Path

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

    /**
     * Gets the link target of this path, or null if it is not a symlink.
     */
    public fun linkTarget(): Path?

    // See: pathlib.Path.resolve()
    /**
     * Resolves a path into an absolute path, following symlinks, returning the new resolved path.
     *
     * If [strict] is true, the path must exist and [FileNotFoundException] will be raised if it
     * is not. If [strict] is false, the path will be resolved as far as possible.
     */
    public fun resolveFully(strict: Boolean = true): Path

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
     * Scans this directory, passing a [DirEntry] to the specified block. This is a safer and
     * faster alternative to [listDir] as a new list does not need to be allocated, and dir
     * entries are fresh.
     */
    public fun scanDir(block: (DirEntry) -> Unit)

    /**
     * Lists the files in this directory.
     *
     * This is equivalent to
     * ``val list = mutableListOf<DirEntry>(); path.scanDir { list.add(it) }``.
     */
    public fun listDir(): List<DirEntry> {
        val items = mutableListOf<DirEntry>()
        scanDir(items::add)
        return items
    }

    /**
     * Moves the file or folder at this path to the new path [path], returning the new path.
     *
     * This function is marked as unsafe as it can fail on some systems when moving between
     * filesystems.
     */
    @Unsafe
    public fun rename(path: PurePath): Path

    /**
     * Checks if it is safe to rename this path to the other [path].
     *
     * This method is used for e.g. Linux filesystems. It does not check things like directory
     * status.
     */
    public fun isSafeToRename(path: Path): Boolean

    /**
     * Copies this file at this path to the new path [Path], returning the new path.
     *
     * For copying empty directories, use [createDirectory]. For copying directories recursively,
     * see [tf.lotte.tinlok.fs.path.recursiveCopy].
     */
    public fun copyFile(path: PurePath): Path

    /**
     * Creates a new symbolic link at this path pointing to the specified other [path].
     */
    public fun symlinkTo(path: Path)

    /**
     * Removes this directory. It must be empty.
     */
    public fun removeDirectory()

    /**
     * Deletes this file or symbolic link.
     */
    public fun unlink()

    /**
     * Opens this path for I/O operations, using the specified [modes].
     *
     * This function is marked as unsafe as it can leak open files. See the safe
     * [tf.lotte.tinlok.fs.path.open] instead.
     */
    @Unsafe
    public fun unsafeOpen(vararg modes: FileOpenMode): FilesystemFile
}
