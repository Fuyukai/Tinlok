/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste.fs.path

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import platform.posix.*
import tf.lotte.kste.ByteString
import tf.lotte.kste.fs.FilePermission
import tf.lotte.kste.fs.PosixFilePermission
import tf.lotte.kste.fs.Stat
import tf.lotte.kste.fs.or
import tf.lotte.kste.readZeroTerminated

/**
 * Linux-based implementation of a [Path]. Wraps a [PosixPurePath].
 */
internal class LinuxPath(private val pure: PosixPurePath) : Path {
    companion object {
        const val S_IFMT = 61440U
        const val S_ISDIR = 16384U
        const val S_ISLNK = 40960U
        const val S_ISREG = 32768U
    }

    // == purepath functionality == //
    override val isAbsolute: Boolean by pure::isAbsolute
    override val parent: LinuxPath get() = LinuxPath(pure.parent)
    override val rawComponents: List<ByteString> by pure::rawComponents
    override val components: List<String> by pure::components
    override val rawName: ByteString by pure::rawName
    override val name: String by pure::name
    override fun join(other: PurePath): LinuxPath = LinuxPath(pure.join(other))
    override fun join(other: ByteString): LinuxPath = LinuxPath(pure.join(other))

    override fun extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails(): String {
        return pure.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
    }

    // == path functionality == //
    override fun exists(): Boolean {
        val strPath = pure.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
        return access(strPath, F_OK) != -1
    }

    override fun stat(followSymlinks: Boolean): Stat? = memScoped {
        val strPath = extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
        val pathStat = alloc<stat>()
        val res =
            if (followSymlinks) stat(strPath, pathStat.ptr)
            else lstat(strPath, pathStat.ptr)

        if (res == -1) {
            if (errno == ENOENT) return null
            else TODO("stat errno")
        }

        return Stat(
            ownerUID = pathStat.st_uid.toInt(),
            ownerGID = pathStat.st_gid.toInt(),
            size = pathStat.st_size,
            st_mode = pathStat.st_mode
        )
    }

    override fun isDirectory(followSymlinks: Boolean): Boolean =
        stat(followSymlinks)?.isDirectory ?: false
    override fun isFile(followSymlinks: Boolean): Boolean =
        stat(followSymlinks)?.isFile ?: false
    override fun isLink(): Boolean =
        stat(followSymlinks = false)?.isLink ?: false

    override fun createDirectory(
        parents: Boolean,
        existOk: Boolean,
        vararg permissions: FilePermission
    ) {
        val path = this.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
        val permMask = if (permissions.isEmpty()) {
            0
        } else {
            permissions
                .filterIsInstance<PosixFilePermission>()
                .map { it.bit }
                .reduce { acc, i -> acc or i }
        }.toUInt()

        if (parents) {
            for (parent in allParents().reversed()) {
                // this should always be fine on linux
                val p = parent as LinuxPath
                p.createDirectory(parents = false, existOk = true, *permissions)
            }
        }

        val res = mkdir(path, permMask)
        if (res != 0) {
            // if it exists, we're all good
            if (errno == EEXIST && existOk) return
            // if not, handle errno
            TODO("mkdir errno")
        }
    }

    override fun listFiles(): List<Path> {
        val path = this.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
        val dir = opendir(path) ?: TODO("opendir failed")
        val items = mutableListOf<Path>()

        try {
            while (true) {
                val next = readdir(dir) ?: break
                val item = next.pointed
                val name = item.d_name.readZeroTerminated(256)
                val bs = ByteString.fromByteArray(name)
                items.add(join(bs))
            }

            return items
        } finally {
            closedir(dir)
        }
    }

    override fun removeDirectory() {
        val path = this.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
        val res = rmdir(path)
        if (res != 0) {
            TODO("rmdir errno")
        }
    }

    override fun unlink() {
        val path = this.extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails()
        val res = unlink(path)
        if (res != 0) {
            TODO("unlink errno")
        }
    }
}
