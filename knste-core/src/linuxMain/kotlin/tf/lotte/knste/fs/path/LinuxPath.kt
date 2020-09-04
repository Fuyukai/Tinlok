/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import platform.posix.F_OK
import tf.lotte.knste.ByteString
import tf.lotte.knste.fs.*
import tf.lotte.knste.impls.Syscall
import tf.lotte.knste.readZeroTerminated
import tf.lotte.knste.util.Unsafe

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

    @Unsafe
    override fun unsafeToString(): String {
        return pure.unsafeToString()
    }

    // == path functionality == //
    @OptIn(Unsafe::class)
    override fun exists(): Boolean {
        val strPath = pure.unsafeToString()
        return Syscall.access(strPath, F_OK)
    }

    @OptIn(Unsafe::class)
    override fun stat(followSymlinks: Boolean): Stat? = memScoped {
        val strPath = pure.unsafeToString()
        val pathStat = Syscall.stat(this, strPath, followSymlinks)

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

    @OptIn(Unsafe::class)
    override fun createDirectory(
        parents: Boolean,
        existOk: Boolean,
        vararg permissions: FilePermission
    ) {
        val path = this.unsafeToString()
        val permMask = if (permissions.isEmpty()) {
            PosixFilePermission.ALL.bit
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

        Syscall.mkdir(path, permMask, existOk)
    }

    @OptIn(Unsafe::class)
    override fun listFiles(): List<Path> {
        val path = this.unsafeToString()
        val dir = Syscall.opendir(path)
        val items = mutableListOf<Path>()

        try {
            while (true) {
                val next = Syscall.readdir(dir) ?: break
                val item = next.pointed
                val name = item.d_name.readZeroTerminated(256)
                val bs = ByteString.fromByteArray(name)
                items.add(join(bs))
            }

            return items
        } finally {
            Syscall.closedir(dir)
        }
    }

    @OptIn(Unsafe::class)
    override fun removeDirectory() {
        val path = this.unsafeToString()
        Syscall.rmdir(path)
    }

    @OptIn(Unsafe::class)
    override fun unlink() {
        val path = this.unsafeToString()
        Syscall.unlink(path)
    }

    override fun open(vararg modes: FileOpenMode): FilesystemFile {
        return LinuxSyncFile(this, modes.toSet())
    }
}
