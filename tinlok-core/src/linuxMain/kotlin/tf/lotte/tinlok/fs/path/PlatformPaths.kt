/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.posix.PATH_MAX
import platform.posix.getenv
import platform.posix.mkdtemp
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.system.readZeroTerminated
import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.types.bytestring.b
import tf.lotte.tinlok.util.Unsafe

public actual typealias PlatformPurePath = PosixPurePath

/**
 * Linux implementation of the platform pathing.
 */
public actual object PlatformPaths {
    @Suppress("unused")
    public actual val pathSeparator: ByteString = b("/")

    @OptIn(Unsafe::class)
    public actual fun cwd(): Path {
        val path = ByteString.fromByteArray(Syscall.getcwd())
        val pure = PosixPurePath.fromByteString(path)
        return LinuxPath(pure)
    }

    @OptIn(Unsafe::class)
    public actual fun home(): Path {
        val path = memScoped {
            val env = getenv("HOME")
            if (env != null) {
                ByteString.fromByteArray(env.readZeroTerminated())
            } else {
                val uid = Syscall.getuid()
                val passwd = Syscall.getpwuid_r(this, uid) ?: error("User has no passwd entry")
                val name = passwd.pw_dir?.readZeroTerminated(PATH_MAX)
                    ?: error("User has no home directory")
                ByteString.fromByteArray(name)
            }
        }

        val pure = PosixPurePath.fromByteString(path)
        return LinuxPath(pure)
    }

    public actual fun purePath(of: ByteString): PosixPurePath {
        return PosixPurePath.fromByteString(of)
    }


    public actual fun path(of: ByteString): Path {
        return LinuxPath(PosixPurePath.fromByteString(of))
    }

    public actual fun path(of: PosixPurePath): Path {
        return LinuxPath(of)
    }

    /**
     * Creates a new temporary directory, returning its [Path].
     */
    @Unsafe
    public actual fun makeTempDirectory(prefix: String): Path {
        // lol at this function literally replacing XXXXXX
        // ALSO THIS CORRUPTS MEMORY IF YOU DON'T PIN IT
        val template = "/tmp/$prefix-XXXXXX".encodeToByteArray()
        val path = template.usePinned {
            mkdtemp(it.addressOf(0)) ?: TODO("mkdtemp error")
        }

        val ba = path.readZeroTerminated(PATH_MAX)
        val bs = ByteString.fromByteArray(ba)
        return path(bs)
    }

}
