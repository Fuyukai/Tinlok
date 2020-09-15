/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.F_OK
import platform.posix.O_CREAT
import platform.posix.O_RDONLY
import platform.posix.O_WRONLY
import tf.lotte.tinlok.ByteString
import tf.lotte.tinlok.b
import tf.lotte.tinlok.exc.FileNotFoundException
import tf.lotte.tinlok.fs.*
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.system.readZeroTerminated
import tf.lotte.tinlok.util.Unsafe

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
    override fun resolve(strict: Boolean): Path {
        if (!strict) TODO("Pure-Kotlin resolving")
        val realPath = memScoped { Syscall.realpath(this, pure.unsafeToString()) }
        return LinuxPath(PosixPurePath.fromByteString(realPath))
    }

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

    @Unsafe
    override fun rename(path: PurePath): Path {
        Syscall.rename(unsafeToString(), path.unsafeToString())
        return path.ensureLinuxPath()
    }

    /**
     * Efficiently copies from this path using sendfile().
     */
    @OptIn(Unsafe::class)
    override fun copy(path: PurePath): Path {
        // sendfile to B from A which does an efficient copy
        val to = Syscall.open(path.unsafeToString(), O_WRONLY or O_CREAT)
        val from = Syscall.open(this.unsafeToString(), O_RDONLY)
        try {
            Syscall.sendfile(to, from, size().toULong())
        } finally {
            Syscall.__closeall(to, from)
        }
        return path.ensureLinuxPath()
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
    return try {
        stat(followSymlinks)
    } catch (e: FileNotFoundException) {
        null
    }
}

private inline fun PurePath.ensureLinuxPath(): LinuxPath {
    if (this is LinuxPath) return this
    if (this !is PosixPurePath) error("Not a POSIX path!")
    return LinuxPath(this)
}
