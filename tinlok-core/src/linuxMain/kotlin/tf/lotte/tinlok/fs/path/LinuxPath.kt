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
import platform.posix.*
import tf.lotte.tinlok.ByteString
import tf.lotte.tinlok.b
import tf.lotte.tinlok.exc.FileNotFoundException
import tf.lotte.tinlok.exc.IsADirectoryException
import tf.lotte.tinlok.exc.OSException
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
    override val rawAnchor: ByteString? by pure::rawAnchor
    override val anchor: String? by pure::anchor
    override val rawName: ByteString by pure::rawName
    override val name: String by pure::name
    override fun resolveChild(other: PurePath): LinuxPath = LinuxPath(pure.resolveChild(other))
    override fun withName(name: ByteString): LinuxPath = LinuxPath(pure.withName(name))

    @Unsafe
    override fun unsafeToString(): String = pure.unsafeToString()

    // todo: make this use LinuxPath as the prefix
    override fun toString(): String = pure.toString()
    override fun isChildOf(other: PurePath): Boolean = pure.isChildOf(other)
    override fun reparent(from: PurePath, to: PurePath): LinuxPath =
        LinuxPath(pure.reparent(from, to))

    // == path functionality == //
    @OptIn(Unsafe::class)
    override fun exists(): Boolean {
        val strPath = pure.unsafeToString()
        return Syscall.access(strPath, F_OK)
    }

    @OptIn(Unsafe::class)
    fun stat(followSymlinks: Boolean): Stat = memScoped {
        val strPath = pure.unsafeToString()
        val pathStat = Syscall.stat(this, strPath, followSymlinks)

        return Stat(
            ownerUID = pathStat.st_uid.toInt(),
            ownerGID = pathStat.st_gid.toInt(),
            size = pathStat.st_size,
            deviceId = pathStat.st_dev,
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

    @OptIn(Unsafe::class)
    override fun isDirectory(followSymlinks: Boolean): Boolean =
        statSafe(followSymlinks)?.isDirectory ?: false

    @OptIn(Unsafe::class)
    override fun isRegularFile(followSymlinks: Boolean): Boolean =
        statSafe(followSymlinks)?.isFile ?: false

    @OptIn(Unsafe::class)
    override fun isLink(): Boolean =
        statSafe(followSymlinks = false)?.isLink ?: false

    @OptIn(Unsafe::class)
    override fun size(): Long = stat(followSymlinks = false).size

    @OptIn(Unsafe::class)
    override fun linkTarget(): Path? = memScoped {
        // safer approach
        return try {
            val link = Syscall.readlink(this, unsafeToString())
            LinuxPath(PosixPurePath.fromByteString(link))
        } catch (e: FileNotFoundException) {
            null
        } catch (e: OSException) {
            if (e.errno == EINVAL) null
            else throw e
        }
    }

    @OptIn(Unsafe::class)
    override fun toAbsolutePath(strict: Boolean): Path {
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

                items.add(resolveChild(bs))
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

    @OptIn(Unsafe::class)
    override fun isSafeToRename(path: Path): Boolean {
        // obviously can't rename
        if (path !is LinuxPath) return false

        // equally can't rename if we don't exist
        val dev = this.statSafe(followSymlinks = false)?.deviceId ?: return false

        // find the device ID of the other path
        // first we check the other file directly, not following symlinks as a rename would just
        // overwrite the symlink
        val otherPathStat = path.statSafe(followSymlinks = false)
        if (otherPathStat != null) {
            // rename(2) doesn't work across filesystems (!!!) so different device IDs means it
            // would fail
            return otherPathStat.deviceId == dev
        }

        // if it's null, we want to check the parent but we do follow the symlink as we'd be
        // renaming over the file in the real location
        val parentStat = path.parent.statSafe(followSymlinks = true)
        if (parentStat != null) {
            return parentStat.deviceId == dev
        }

        // either the file or the parent directory doesn't exist
        // so we obviously cannot rename over it
        return false
    }

    /**
     * Efficiently copies from this path using sendfile().
     */
    @OptIn(Unsafe::class)
    override fun copyFile(path: PurePath): Path {
        // open() doesn't fail on directories (with O_RDONLY)...
        // so we have to check it ourselves
        if (this.isDirectory(followSymlinks = true)) {
            throw IsADirectoryException(unsafeToString())
        }

        // sendfile to B from A which does an efficient copy
        // also, get the correct permissions from the original file
        val perms = stat(followSymlinks = false).permBits
        val to = Syscall.open(path.unsafeToString(), O_WRONLY or O_CREAT, perms.toInt())
        // always close to if opening from errors
        val from = try {
            Syscall.open(this.unsafeToString(), O_RDONLY)
        } catch (e: Throwable) {
            Syscall.close(to)
            throw e
        }

        try {
            Syscall.sendfile(to, from, size().toULong())
        } finally {
            // always close both fds
            Syscall.__closeall(to, from)
        }
        return path.ensureLinuxPath()
    }

    @OptIn(Unsafe::class)
    override fun symlinkTo(path: Path) {
        Syscall.symlink(path.unsafeToString(), this.unsafeToString())
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
        if (this.isDirectory(followSymlinks = false)) {
            throw IsADirectoryException(unsafeToString())
        }

        return LinuxSyncFile(this, modes.toSet())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Path) return false

        return pure.rawComponents == other.rawComponents
    }

    override fun hashCode(): Int {
        return pure.hashCode()
    }
}

/**
 * Helper function for safe stat.
 */
@OptIn(Unsafe::class)
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
