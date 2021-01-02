/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import platform.windows.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.fs.*
import tf.lotte.tinlok.io.FileNotFoundException
import tf.lotte.tinlok.io.IsADirectoryException
import tf.lotte.tinlok.system.*
import tf.lotte.tinlok.util.ByteString
import tf.lotte.tinlok.util.toByteString
import tf.lotte.tinlok.util.use

/**
 * Windows implementation of a path.
 */
public class WindowsPath(
    driveLetter: ByteString?,
    volume: ByteString?,
    rest: List<ByteString>,
) : Path, WindowsPurePath(driveLetter, volume, rest) {
    public companion object {
        /**
         * Creates a new [WindowsPath] from a [ByteString].
         */
        public fun fromByteString(bs: ByteString): WindowsPath {
            val (letter, volume, rest) = parsePath(bs)
            return WindowsPath(letter, volume, rest)
        }

        /**
         * Creates a new [WindowsPath] from a [WindowsPurePath].
         */
        public fun fromPurePath(purePath: WindowsPurePath): WindowsPath {
            return WindowsPath(purePath.driveLetter, purePath.volume, purePath.rest)
        }
    }

    // == purepath functionality == //
    override fun resolveChild(other: PurePath): WindowsPath {
        val pure = super.resolveChild(other)
        return WindowsPath(pure.driveLetter, pure.volume, pure.rest)
    }

    override fun withName(name: ByteString): WindowsPath {
        val pure = super.withName(name)
        return WindowsPath(pure.driveLetter, pure.volume, pure.rest)
    }

    override fun reparent(from: PurePath, to: PurePath): WindowsPath {
        val pure = super.reparent(from, to)
        return WindowsPath(pure.driveLetter, pure.volume, pure.rest)
    }

    override val parent: WindowsPath
        get() {
            val parent = super.parent
            return WindowsPath(driveLetter, volume, parent.rest)
        }

    // == path functionality == //
    /**
     * Checks to see if this [Path] exists.
     */
    @OptIn(Unsafe::class)
    override fun exists(): Boolean {
        return Syscall.PathFileExists(unsafeToString())
    }

    /**
     * Gets the attributes of a file, optionally following symlinks.
     */
    @Unsafe
    private fun getAttributesSafe(followSymlinks: Boolean): FileAttributes? {
        return if (followSymlinks) {
            val realPath = Syscall.__real_path(unsafeToString())

            // weird, gross, ew
            if (realPath == null) {
                Syscall.__get_attributes_safer(unsafeToString())
            } else {
                Syscall.__get_attributes_safer(realPath)
            }
        } else {
            Syscall.__get_attributes_safer(unsafeToString())
        }
    }

    /**
     * Checks if this [Path] is a directory.
     */
    @OptIn(Unsafe::class)
    override fun isDirectory(followSymlinks: Boolean): Boolean {
        return getAttributesSafe(followSymlinks)?.isDirectory ?: false
    }

    /**
     * Checks if this [Path] is a regular file.
     */
    @OptIn(Unsafe::class)
    override fun isRegularFile(followSymlinks: Boolean): Boolean {
        return getAttributesSafe(followSymlinks)?.isRegularFile ?: false
    }

    /**
     * Checks if this [Path] is a symbolic link.
     */
    @OptIn(Unsafe::class)
    override fun isLink(): Boolean {
        return getAttributesSafe(followSymlinks = false)?.isSymlink() ?: false
    }

    /**
     * Gets the size of this file.
     */
    @OptIn(Unsafe::class, ExperimentalUnsignedTypes::class)
    override fun size(): Long {
        return Syscall.GetFileAttributesEx(unsafeToString()).size.toLong()
    }

    /**
     * Gets the link target of this path, or null if it is not a symlink.
     */
    @OptIn(Unsafe::class)
    override fun linkTarget(): WindowsPath? {
        val target = Syscall.__real_path(unsafeToString()) ?: return null

        // gross!
        val (letter, volume, rest) = parsePath(target.toByteString())
        val path = WindowsPath(letter, volume, rest)

        return if (path == this) null
        else path
    }

    /**
     * Resolves a path into an absolute path, following symlinks, returning the new resolved path.
     *
     * If [strict] is true, the path must exist and [FileNotFoundException] will be raised if it
     * is not. If [strict] is false, the path will be resolved as far as possible.
     */
    @OptIn(Unsafe::class)
    override fun resolveFully(strict: Boolean): WindowsPath {
        val realPath = Syscall.__real_path(unsafeToString())
        val strpath = realPath ?: Syscall.GetFullPathName(unsafeToString())

        val (letter, volume, rest) = parsePath(strpath.toByteString())
        val path = WindowsPath(letter, volume, rest)

        if (strict && !path.exists()) {
            throw FileNotFoundException(path.toByteString())
        } else {
            return path
        }
    }

    /**
     * Gets the owner username for this file, or null if this file doesn't have an owner (some
     * filesystems or virtual filesystems).
     */
    override fun owner(followSymlinks: Boolean): String? {
        TODO("Not yet implemented")
    }

    /**
     * Creates this [Path] as a directory. If [parents] is true, creates all the parent
     * directories too. If [existOk] is true, then an existing directory will not cause an error.
     * The directory will be created with the [permissions] file permissions.
     */
    @OptIn(Unsafe::class)
    override fun createDirectory(
        parents: Boolean, existOk: Boolean, vararg permissions: FilePermission,
    ) {
        if (parents) {
            for (parent in allParents().reversed()) {
                // this should always be correct
                val p = parent as WindowsPath
                p.createDirectory(parents = false, existOk = true, *permissions)
            }
        }

        Syscall.CreateDirectory(unsafeToString(), existOk)
    }

    /**
     * Scans this directory, passing a [DirEntry] to the specified block. This is a safer and
     * faster alternative to [listDir] as a new list does not need to be allocated, and dir
     * entries are fresh.
     */
    @OptIn(Unsafe::class)
    override fun scanDir(block: (DirEntry) -> Unit) {
        DirectoryScanContext(this).use {
            while (true) {
                val next = it.next() ?: break
                block(next)
            }
        }
    }

    /**
     * Moves the file or folder at this path to the new path [path], returning the new path.
     *
     * This function is marked as unsafe as it can fail on some systems when moving between
     * filesystems.
     */
    @OptIn(Unsafe::class)
    override fun rename(path: PurePath): Path {
        require(path is WindowsPurePath) { "Can only rename to a Windows path!" }
        Syscall.MoveFile(unsafeToString(), path.unsafeToString())
        return fromPurePath(path)
    }

    /**
     * Checks if it is safe to rename this path to the other [path].
     *
     * This method is used for e.g. Linux filesystems. It does not check things like directory
     * status.
     */
    override fun isSafeToRename(path: Path): Boolean {
        // MoveFile does a copy + delete for us automatically if needed, so this is always fine
        return true
    }

    /**
     * Copies this file at this path to the new path [Path], returning the new path.
     *
     * For copying empty directories, use [createDirectory]. For copying directories recursively,
     * see [tf.lotte.tinlok.fs.path.recursiveCopy].
     */
    @OptIn(Unsafe::class)
    override fun copyFile(path: PurePath): Path {
        require(path is WindowsPurePath) { "Can only copy to a Windows path!" }
        Syscall.CopyFile(unsafeToString(), path.unsafeToString(), true)
        return fromPurePath(path)
    }

    /**
     * Creates a new symbolic link at this path pointing to the specified other [path].
     */
    @OptIn(Unsafe::class)
    override fun symlinkTo(path: Path) {
        Syscall.CreateSymbolicLink(unsafeToString(), path.unsafeToString())
    }

    /**
     * Removes this directory. It must be empty.
     */
    @OptIn(Unsafe::class)
    override fun removeDirectory() {
        Syscall.RemoveDirectory(unsafeToString())
    }

    /**
     * Deletes this file or symbolic link.
     */
    @OptIn(Unsafe::class)
    override fun unlink() {
        return Syscall.__highlevel_unlink(unsafeToString())
    }

    /**
     * Opens this path for I/O operations, using the specified [modes].
     *
     * This function is marked as unsafe as it can leak open files. See the safe
     * [tf.lotte.tinlok.fs.path.open] instead.
     */
    @Unsafe
    override fun unsafeOpen(vararg modes: FileOpenMode): SynchronousFile {
        if (isDirectory(followSymlinks = true)) {
            throw IsADirectoryException(toByteString())
        }

        val openModes = modes.toSet()

        val accessFlag = when {
            openModes.containsAll(listOf(StandardOpenModes.READ,
                StandardOpenModes.WRITE)) -> (GENERIC_READ.toInt().or(GENERIC_WRITE))
            openModes.contains(StandardOpenModes.READ) -> GENERIC_READ.toInt()
            openModes.contains(StandardOpenModes.WRITE) -> GENERIC_WRITE
            openModes.contains(StandardOpenModes.APPEND) -> GENERIC_WRITE
            else -> 0
        }

        val creationFlag = when {
            openModes.contains(StandardOpenModes.CREATE) -> OPEN_ALWAYS
            openModes.contains(StandardOpenModes.CREATE_NEW) -> platform.windows.CREATE_NEW
            else -> OPEN_EXISTING
        }

        @OptIn(Unsafe::class)
        val handle = Syscall.CreateFile(unsafeToString(), accessFlag, creationFlag, 0)
        // seek to end
        if (openModes.contains(StandardOpenModes.APPEND)) {
            Syscall.SetFilePointer(handle, 0, SeekWhence.END)
        }

        return SynchronousFilesystemFile(this, FILE(handle))
    }
}
