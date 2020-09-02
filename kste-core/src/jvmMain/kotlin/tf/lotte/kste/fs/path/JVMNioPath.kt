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
import tf.lotte.kste.Sys
import tf.lotte.kste.fs.FilePermission
import tf.lotte.kste.fs.Stat
import java.nio.file.Files
import java.nio.file.LinkOption
import kotlin.streams.toList

/**
 * Implements [Path] via NIO APIs.
 */
internal class JVMNioPath(private val pure: PurePath) : Path {
    // == purepath == //
    override val rawName: ByteString by pure::rawName
    override val name: String by pure::name
    override val isAbsolute: Boolean by pure::isAbsolute
    override val rawComponents: List<ByteString> by pure::rawComponents
    override val components: List<String> by pure::components
    override val parent: Path by lazy { JVMNioPath(pure.parent) }
    override fun join(other: PurePath): JVMNioPath = JVMNioPath(pure.join(other))
    override fun join(other: ByteString): JVMNioPath = JVMNioPath(pure.join(other))

    override fun extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails(): String {
        return pure.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
    }

    private val _nioPath: java.nio.file.Path by lazy {
        val first = components.first()
        val rest = components.drop(1).toTypedArray()
        java.nio.file.Path.of(first, *rest)
    }

    /**
     * Converts this Path to an NIO Path.
     */
    public fun toNioPath(): java.nio.file.Path = _nioPath

    // == path == //
    override fun exists(): Boolean {
        return Files.exists(_nioPath)
    }

    override fun isFile(followSymlinks: Boolean): Boolean {
        return if (followSymlinks) Files.isRegularFile(_nioPath)
        else Files.isRegularFile(toNioPath(), LinkOption.NOFOLLOW_LINKS)
    }

    override fun isDirectory(followSymlinks: Boolean): Boolean {
        return if (followSymlinks) Files.isDirectory(_nioPath)
        else Files.isDirectory(_nioPath, LinkOption.NOFOLLOW_LINKS)
    }

    override fun isLink(): Boolean {
        return Files.isSymbolicLink(_nioPath)
    }

    override fun createDirectory(
        parents: Boolean, existOk: Boolean, vararg permissions: FilePermission
    ) {
        if (parents) {
            Files.createDirectories(_nioPath)
        } else {
            Files.createDirectory(_nioPath)
        }
    }

    override fun stat(followSymlinks: Boolean): Stat? {
        if (!exists()) return null

        val options = if (followSymlinks) arrayOf() else arrayOf(LinkOption.NOFOLLOW_LINKS)

        val uid =
            if (Sys.osInfo.isWindows) 0
            else Files.getAttribute(_nioPath, "unix:uid", *options) as Int

        val gid =
            if (Sys.osInfo.isWindows) 0
            else Files.getAttribute(_nioPath, "unix:gid", *options) as Int

        val stMode = Files.getAttribute(_nioPath, "unix:mode", *options) as Int
        val size = Files.size(_nioPath)

        return Stat(ownerUID = uid, ownerGID = gid, size = size, st_mode = stMode.toUInt())
    }

    override fun listFiles(): List<Path> =
        Files.list(_nioPath).map { Paths.fromNioPath(it) }.toList()

    override fun removeDirectory() {
        Files.delete(_nioPath)
    }

    override fun unlink() {
        Files.delete(_nioPath)
    }

}
