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
import tf.lotte.cc.types.ByteString
import tf.lotte.tinlok.fs.*

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

    override fun exists(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDirectory(followSymlinks: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun isRegularFile(followSymlinks: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLink(): Boolean {
        TODO("Not yet implemented")
    }

    override fun size(): Long {
        TODO("Not yet implemented")
    }

    override fun linkTarget(): Path? {
        TODO("Not yet implemented")
    }

    override fun toAbsolutePath(strict: Boolean): Path {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
