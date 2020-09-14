/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import kotlinx.cinterop.*
import platform.posix.PATH_MAX
import platform.posix.getcwd
import platform.posix.getenv
import platform.posix.mkdtemp
import tf.lotte.knste.ByteString
import tf.lotte.knste.b
import tf.lotte.knste.system.Syscall
import tf.lotte.knste.system.readZeroTerminated
import tf.lotte.knste.util.Unsafe

@PublishedApi
internal actual object PlatformPaths {
    public actual val pathSeparator: ByteString = b("/")

    // TODO: Syscall wrapper
    @OptIn(Unsafe::class)
    public actual fun cwd(): Path {
        val path = memScoped {
            val buf = allocArray<ByteVar>(PATH_MAX)
            val res = getcwd(buf, PATH_MAX) ?: TODO("Throw errno")
            val ba = res.readZeroTerminated(PATH_MAX)
            ByteString.fromByteArray(ba)
        }

        val pure = PosixPurePath.fromByteString(path)
        return LinuxPath(pure)
    }

    // TODO: Syscall wrapper
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

    public actual fun purePath(of: ByteString): PurePath {
        return PosixPurePath.fromByteString(of)
    }


    public actual fun path(of: ByteString): Path {
        return LinuxPath(PosixPurePath.fromByteString(of))
    }

    public actual fun path(of: PurePath): Path {
        require(of is PosixPurePath) { "Can't create a real path out of a non-POSIX path" }
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
