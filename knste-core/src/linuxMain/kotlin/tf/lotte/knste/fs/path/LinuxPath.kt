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
import kotlinx.cinterop.toKString
import platform.posix.F_OK
import tf.lotte.knste.ByteString
import tf.lotte.knste.b
import tf.lotte.knste.exc.FileNotFoundException
import tf.lotte.knste.fs.*
import tf.lotte.knste.impls.Syscall
import tf.lotte.knste.readZeroTerminated
import tf.lotte.knste.util.Unsafe

/**
 * Linux-based implementation of a [Path]. Wraps a [PosixPurePath].
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal class LinuxPath(private val pure: PosixPurePath) : Path {
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

    override fun toString(): String {
        return pure.toString()
    }

    // == path functionality == //
    @OptIn(Unsafe::class)
    override fun exists(): Boolean {
        val strPath = pure.unsafeToString()
        return Syscall.access(strPath, F_OK)
    }


    @OptIn(Unsafe::class)
    public fun stat(followSymlinks: Boolean): Stat = memScoped {
        val strPath = pure.unsafeToString()
        val pathStat = Syscall.stat(this, strPath, followSymlinks)

        return Stat(
            ownerUID = pathStat.st_uid.toInt(),
            ownerGID = pathStat.st_gid.toInt(),
            size = pathStat.st_size,
            st_mode = pathStat.st_mode
        )
    }

    @OptIn(Unsafe::class)
    override fun owner(followSymlinks: Boolean): String? = memScoped {
        val strPath = pure.unsafeToString()
        val stat = Syscall.stat(this, strPath, followSymlinks)
        val uid = stat.st_uid
        val passwd = Syscall.getpwuid_r(this, uid)

        passwd?.pw_name?.toKString()
    }

    override fun isDirectory(followSymlinks: Boolean): Boolean =
        statSafe(followSymlinks)?.isDirectory ?: false

    override fun isRegularFile(followSymlinks: Boolean): Boolean =
        statSafe(followSymlinks)?.isFile ?: false

    override fun isLink(): Boolean =
        statSafe(followSymlinks = false)?.isLink ?: false

    override fun size(): Long = stat(followSymlinks = false).size

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
    override fun listDir(): List<Path> {
        val path = this.unsafeToString()
        val dir = Syscall.opendir(path)
        val items = mutableListOf<Path>()

        val dot = b(".")
        val dotdot = b("..")

        try {
            while (true) {
                val next = Syscall.readdir(dir) ?: break
                val item = next.pointed
                val name = item.d_name.readZeroTerminated(256)
                val bs = ByteString.fromByteArray(name)

                // skip dot entries (this dir)
                // skip dotdot entries (up a dir)
                if (bs == dot) continue
                if (bs == dotdot) continue

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

    @Unsafe
    override fun unsafeOpen(vararg modes: FileOpenMode): FilesystemFile {
        return LinuxSyncFile(this, modes.toSet())
    }
}

/**
 * Helper function for safe stat.
 */
private inline fun LinuxPath.statSafe(followSymlinks: Boolean): Stat? {
    return try { stat(followSymlinks) }
    catch (e: FileNotFoundException) { null }
}
