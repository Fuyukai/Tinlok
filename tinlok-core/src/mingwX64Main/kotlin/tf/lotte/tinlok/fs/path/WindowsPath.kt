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
import tf.lotte.cc.exc.IsADirectoryException
import tf.lotte.cc.types.ByteString
import tf.lotte.cc.types.toByteString
import tf.lotte.cc.use
import tf.lotte.tinlok.fs.*
import tf.lotte.tinlok.system.DirectoryScanContext
import tf.lotte.tinlok.system.FileAttributes
import tf.lotte.tinlok.system.Syscall

internal class WindowsPath(
    driveLetter: ByteString?,
    volume: ByteString?,
    rest: List<ByteString>
) : Path, WindowsPurePath(driveLetter, volume, rest) {
    companion object {
        /**
         * Creates a new [WindowsPath] from a [ByteString].
         */
        fun fromByteString(bs: ByteString): WindowsPath {
            val (letter, volume, rest) = parsePath(bs)
            return WindowsPath(letter, volume, rest)
        }

        /**
         * Creates a new [WindowsPath] from a [WindowsPurePath].
         */
        fun fromPurePath(purePath: WindowsPurePath): WindowsPath {
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

    override val parent: WindowsPath get() {
        val parent = super.parent
        return WindowsPath(driveLetter, volume, parent.rest)
    }

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

    @OptIn(Unsafe::class)
    override fun isDirectory(followSymlinks: Boolean): Boolean {
        return getAttributesSafe(followSymlinks)?.isDirectory ?: false
    }

    @OptIn(Unsafe::class)
    override fun isRegularFile(followSymlinks: Boolean): Boolean {
        return getAttributesSafe(followSymlinks)?.isRegularFile ?: false
    }

    @OptIn(Unsafe::class)
    override fun isLink(): Boolean {
        return getAttributesSafe(followSymlinks = false)?.isSymlink ?: false
    }

    @OptIn(Unsafe::class, ExperimentalUnsignedTypes::class)
    override fun size(): Long {
        return Syscall.GetFileAttributesEx(unsafeToString()).size.toLong()
    }

    @OptIn(Unsafe::class)
    override fun linkTarget(): WindowsPath? {
        if (!isLink()) return null
        val target = Syscall.__real_path(unsafeToString()) ?: return null
        val (letter, volume, rest) = parsePath(target.toByteString())

        return WindowsPath(letter, volume, rest)
    }

    @OptIn(Unsafe::class)
    override fun toAbsolutePath(strict: Boolean): WindowsPath {
        if (isAbsolute) return this
        val strpath = Syscall.GetFullPathName(unsafeToString())
        val (letter, volume, rest) = parsePath(strpath.toByteString())
        val path = WindowsPath(letter, volume, rest)

        if (strict && !path.exists()) {
            throw FileNotFoundException(path.unsafeToString())
        } else {
            return path
        }
    }

    override fun owner(followSymlinks: Boolean): String? {
        TODO("Not yet implemented")
    }

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

    @OptIn(Unsafe::class)
    override fun scanDir(block: (DirEntry) -> Unit) {
        DirectoryScanContext(this).use {
            while (true) {
                val next = it.next() ?: break
                block(next)
            }
        }
    }

    @Unsafe
    override fun rename(path: PurePath): Path {
        require(path is WindowsPurePath) { "Can only rename to a Windows path!" }
        Syscall.MoveFile(unsafeToString(), path.unsafeToString())
        return fromPurePath(path)
    }

    override fun isSafeToRename(path: Path): Boolean {
        // MoveFile does a copy + delete for us automatically if needed, so this is always fine
        return true
    }

    override fun copyFile(path: PurePath): Path {
        TODO("Not yet implemented")
    }

    override fun symlinkTo(path: Path) {
        TODO("Not yet implemented")
    }

    @OptIn(Unsafe::class)
    override fun removeDirectory() {
        Syscall.RemoveDirectory(unsafeToString())
    }

    @OptIn(Unsafe::class)
    override fun unlink() {
        Syscall.DeleteFile(unsafeToString())
    }

    @Unsafe
    override fun unsafeOpen(vararg modes: FileOpenMode): FilesystemFile {
        if (isDirectory(followSymlinks = true)) {
            throw IsADirectoryException(unsafeToString())
        }
        return WindowsSyncFile(this, modes)
    }
}
