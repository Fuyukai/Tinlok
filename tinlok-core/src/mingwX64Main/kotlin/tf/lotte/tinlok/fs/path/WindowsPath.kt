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
import tf.lotte.tinlok.system.FileAttributes
import tf.lotte.tinlok.system.Syscall

internal class WindowsPath(
    driveLetter: ByteString?,
    volume: ByteString?,
    rest: List<ByteString>
) : Path, WindowsPurePath(driveLetter, volume, rest) {
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
            val realPath = Syscall.__symlink_real_path(unsafeToString()) ?: return null
            Syscall.__get_attributes_safer(realPath)
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
        val target = Syscall.__symlink_real_path(unsafeToString()) ?: return null
        val pure = fromString(target)

        return WindowsPath(pure.driveLetter, pure.volume, pure.rest)
    }

    @OptIn(Unsafe::class)
    override fun toAbsolutePath(strict: Boolean): WindowsPath {
        if (isAbsolute) return this
        val strpath = Syscall.GetFullPathName(unsafeToString())
        val pure = fromString(strpath)
        val path = WindowsPath(pure.driveLetter, pure.volume, pure.rest)

        if (strict && !path.exists()) {
            throw FileNotFoundException(path.unsafeToString())
        } else {
            return path
        }
    }

    override fun owner(followSymlinks: Boolean): String? {
        TODO("Not yet implemented")
    }

    override fun createDirectory(
        parents: Boolean, existOk: Boolean, vararg permissions: FilePermission,
    ) {
        TODO("Not yet implemented")
    }

    override fun scanDir(block: (DirEntry) -> Unit) {
        TODO("Not yet implemented")
    }

    @Unsafe
    override fun rename(path: PurePath): Path {
        TODO("Not yet implemented")
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

    override fun removeDirectory() {
        TODO("Not yet implemented")
    }

    override fun unlink() {
        TODO("Not yet implemented")
    }

    @Unsafe
    override fun unsafeOpen(vararg modes: FileOpenMode): FilesystemFile {
        TODO("Not yet implemented")
    }
}
