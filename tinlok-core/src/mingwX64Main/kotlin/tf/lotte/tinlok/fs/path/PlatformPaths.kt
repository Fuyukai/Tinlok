/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("RedundantVisibilityModifier")

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.crypto.SecureRandom
import tf.lotte.tinlok.io.FileAlreadyExistsException
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.*

/**
 * Defines the [PurePath] that for the current platform.
 */
public actual typealias PlatformPurePath = WindowsPurePath

/**
 * Helper factory object for creating new [Path] and [PurePath] objects.
 */
public actual object PlatformPaths {
    /** The path separator that separates individual path components. */
    public actual val pathSeparator: ByteString
        get() = b("\\")

    /**
     * Creates a new [PurePath] for the current working directory.
     */
    @OptIn(Unsafe::class)
    public actual fun cwd(): Path {
        val path = Syscall.GetCurrentDirectory()
        return WindowsPath.fromByteString(path.toByteString())
    }

    /**
     * Creates a new [PurePath] for the current user's home directory.
     */
    @OptIn(Unsafe::class)
    public actual fun home(): Path {
        val path = Syscall.ExpandEnvironmentStrings("%userprofile%")
        return WindowsPath.fromByteString(path.toByteString())
    }

    /**
     * Creates a new platform [Path] from the given [ByteString].
     */
    public actual fun path(of: ByteString): Path {
        return WindowsPath.fromByteString(of)
    }

    /**
     * Creates a new platform Path from the given [PurePath].
     */
    public actual fun path(of: PlatformPurePath): Path {
        return WindowsPath.fromPurePath(of)
    }

    /**
     * Creates a new platform [PurePath] from the given [ByteString].
     */
    public actual fun purePath(of: ByteString): PlatformPurePath {
        return WindowsPurePath.fromByteString(of)
    }

    /**
     * Creates a new temporary directory and returns its path.
     */
    @Unsafe
    public actual fun makeTempDirectory(prefix: String): Path {
        val path = path(Syscall.GetTempPath().toByteString())

        // retry loop to try and find an unused random string
        for (unused in 0..10) {
            val name = prefix + SecureRandom.randomAsciiString(8)
            val newPath = path.resolveChild(name)
            try {
                newPath.createDirectory(parents = false, existOk = false)
                return newPath
            } catch (e: FileAlreadyExistsException) {
                continue
            }
        }

        throw Exception("Unable to create a unique temporary directory")
    }

}
