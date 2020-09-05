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
import platform.posix.*
import tf.lotte.knste.ByteString
import tf.lotte.knste.b
import tf.lotte.knste.readZeroTerminated

public actual object Paths {
    public actual val pathSeparator: ByteString = b("/")

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

    public actual fun home(): Path {
        val path = memScoped {
            val env = getenv("HOME")
            if (env != null) {
                ByteString.fromByteArray(env.readZeroTerminated())
            } else {
                // complicated...
                val uid = getuid()
                val passwd = alloc<passwd>()
                val starResult = allocPointerTo<passwd>()

                var bufSize = sysconf(_SC_GETPW_R_SIZE_MAX)
                if (bufSize == -1L) bufSize = 16384
                val buffer = allocArray<ByteVar>(bufSize)

                val res = getpwuid_r(uid, passwd.ptr, buffer, bufSize.toULong(), starResult.ptr)
                if (starResult.value == null) {
                    TODO("getpwduid_r errored")
                }

                val home = passwd.pw_dir!!.readZeroTerminated(PATH_MAX)
                ByteString.fromByteArray(home)
            }
        }

        val pure = PosixPurePath.fromByteString(path)
        return LinuxPath(pure)
    }

    public actual fun purePath(of: String): PurePath {
        return PosixPurePath.fromString(of)
    }

    public actual fun purePath(of: ByteString): PurePath {
        return PosixPurePath.fromByteString(of)
    }

    public actual fun path(of: String): Path {
        return LinuxPath(PosixPurePath.fromString(of))
    }

    public actual fun path(of: ByteString): Path {
        return LinuxPath(PosixPurePath.fromByteString(of))
    }

    /**
     * Creates a new temporary directory, returning its [Path].
     */
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
