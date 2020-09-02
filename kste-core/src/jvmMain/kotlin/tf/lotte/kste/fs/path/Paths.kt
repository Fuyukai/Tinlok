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
import tf.lotte.kste.toByteString
import java.io.File
import java.nio.file.Files

/**
 * Implements Path operations on the JVM.
 */
public actual object Paths {
    public actual val pathSeparator: ByteString
        get() = File.separator.toByteString()

    /**
     * Creates a new platform [PurePath] from the given [ByteString].
     */
    public actual fun purePath(of: ByteString): PurePath {
        if (Sys.osInfo.isWindows) {
            TODO("Windows pure path")
        } else {
            return PosixPurePath.fromByteString(of)
        }
    }

    /**
     * Creates a new platform [PurePath] from the given [String].
     */
    public actual fun purePath(of: String): PurePath {
        if (Sys.osInfo.isWindows) {
            TODO("Windows pure path")
        } else {
            return PosixPurePath.fromString(of)
        }
    }

    public actual fun path(of: String): Path {
        return JVMNioPath(purePath(of))
    }

    public actual fun path(of: ByteString): Path {
        return JVMNioPath(purePath(of))
    }

    public actual fun cwd(): Path {
        // this is usually correct
        val cwd = java.nio.file.Path.of(".").toAbsolutePath().toString()
        val pure = purePath(cwd)
        return JVMNioPath(pure)
    }

    public actual fun home(): Path {
        val home = System.getProperty("user.home")
        val pure = purePath(home)
        return JVMNioPath(pure)
    }

    public actual fun unsafeMakeTempDirectory(prefix: String): Path {
        val path = Files.createTempDirectory(prefix)
        return fromNioPath(path)
    }

    /**
     * Creates a fresh path from a [java.nio.file.Path].
     */
    public fun fromNioPath(path: java.nio.file.Path): Path {
        val sPath = path.toAbsolutePath().toString()
        val pure = purePath(sPath)
        return JVMNioPath(pure)
    }
}
