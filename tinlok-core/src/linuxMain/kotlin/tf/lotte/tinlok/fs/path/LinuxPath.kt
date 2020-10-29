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
import tf.lotte.cc.Unsafe
import tf.lotte.cc.exc.FileNotFoundException
import tf.lotte.cc.exc.IsADirectoryException
import tf.lotte.cc.types.ByteString
import tf.lotte.cc.types.b
import tf.lotte.tinlok.fs.*
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.system.readZeroTerminated

/**
 * Linux-based implementation of a [Path]. Is also a [PosixPurePath].
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal class LinuxPath(rawParts: List<ByteString>) : Path, PosixPurePath(rawParts) {
    companion object {
        /**
         * Creates a new [LinuxPath]
         */
        internal fun fromByteString(bs: ByteString): LinuxPath {
            return LinuxPath(splitParts(bs))
        }
    }

    // == purepath functionality == //
    override val parent: LinuxPath get() = LinuxPath(super.parent.rawComponents)

    override fun resolveChild(other: PurePath): LinuxPath {
        return LinuxPath(super.resolveChild(other).rawComponents)
    }
    override fun withName(name: ByteString): LinuxPath {
        return LinuxPath(super.withName(name).rawComponents)
    }
    override fun reparent(from: PurePath, to: PurePath): LinuxPath {
        return LinuxPath(super.reparent(from, to).rawComponents)
    }


    // == path functionality == //
    @OptIn(Unsafe::class)
    override fun exists(): Boolean {
        val strPath = unsafeToString()
        return Syscall.access(strPath, F_OK)
    }

    @OptIn(Unsafe::class)
    fun statSafe(followSymlinks: Boolean): Stat? = memScoped {
        val strPath = unsafeToString()
        val pathStat = Syscall.__stat_safer(this, strPath, followSymlinks)
            ?: return null

        return Stat(
            ownerUID = pathStat.st_uid.toInt(),
            ownerGID = pathStat.st_gid.toInt(),
            size = pathStat.st_size,
            deviceId = pathStat.st_dev,
            st_mode = pathStat.st_mode
        )
    }

    @OptIn(Unsafe::class)
    fun stat(followSymlinks: Boolean): Stat = memScoped {
        val strPath = unsafeToString()
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
        val strPath = unsafeToString()
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
            LinuxPath(splitParts(link))
        } catch (e: FileNotFoundException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @OptIn(Unsafe::class)
    override fun resolveFully(strict: Boolean): Path {
        if (!strict) TODO("Pure-Kotlin resolving")
        val realPath = Syscall.realpath(unsafeToString())
        return LinuxPath(splitParts(realPath))
    }

    @OptIn(Unsafe::class)
    override fun createDirectory(
        parents: Boolean,
        existOk: Boolean,
        vararg permissions: FilePermission
    ) {
        val path = unsafeToString()
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
    override fun scanDir(block: (DirEntry) -> Unit) {
        val path = unsafeToString()
        val dir = Syscall.opendir(path)
        try {
            val dot = b(".")
            val dotdot = b("..")

            while (true) {
                val next = Syscall.readdir(dir) ?: break
                val item = next.pointed
                val name = item.d_name.readZeroTerminated(256)
                val bs = ByteString.fromByteArray(name)

                // skip dot entries (this dir)
                // skip dotdot entries (up a dir)
                if (bs == dot) continue
                if (bs == dotdot) continue

                val child = resolveChild(bs)
                val type = FileType.fromPosixType(item.d_type)
                val entry = DirEntry(child, type)
                block(entry)

            }
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

        return super.equals(other)
    }
}

// helper functions
private inline fun PurePath.ensureLinuxPath(): LinuxPath {
    if (this is LinuxPath) return this
    if (this !is PosixPurePath) error("Not a POSIX path!")
    return LinuxPath(rawComponents)
}

// this is a gross map because DT_ entries are not contiguous
private val fileTypeMapping = mapOf(
    DT_BLK to FileType.BLOCK_DEVICE,
    DT_CHR to FileType.CHARACTER_DEVICE,
    DT_DIR to FileType.DIRECTORY,
    DT_FIFO to FileType.FIFO,
    DT_LNK to FileType.SYMLINK,
    DT_REG to FileType.REGULAR_FILE,
    DT_SOCK to FileType.UNIX_SOCKET,
    DT_UNKNOWN to FileType.UNKNOWN,
)

@OptIn(ExperimentalUnsignedTypes::class)
private fun FileType.Companion.fromPosixType(type: UByte): FileType {
    return fileTypeMapping[type.toInt()] ?: FileType.UNKNOWN
}
