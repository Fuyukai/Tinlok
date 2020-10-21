/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.fs.*
import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.util.Unsafe

internal class WindowsPath(private val pure: WindowsPurePath) : Path {
    // == purepath functionality == //
    override val isAbsolute: Boolean by pure::isAbsolute
    override val parent: WindowsPath get() = WindowsPath(pure.parent)
    override val rawComponents: List<ByteString> by pure::rawComponents
    override val components: List<String> by pure::components
    override val rawAnchor: ByteString? by pure::rawAnchor
    override val anchor: String? by pure::anchor
    override val rawName: ByteString? by pure::rawName
    override val name: String? by pure::name
    override fun resolveChild(other: PurePath): WindowsPath = WindowsPath(pure.resolveChild(other))
    override fun withName(name: ByteString): WindowsPath = WindowsPath(pure.withName(name))

    @Unsafe
    override fun unsafeToString(): String = pure.unsafeToString()

    // todo: make this use WindowsPath as the prefix
    override fun toString(): String = pure.toString()
    override fun isChildOf(other: PurePath): Boolean = pure.isChildOf(other)
    override fun reparent(from: PurePath, to: PurePath): WindowsPath =
        WindowsPath(pure.reparent(from, to))

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
