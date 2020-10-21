/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("RedundantVisibilityModifier")

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.util.Unsafe

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
        get() = TODO("not implemented")

    /**
     * Creates a new [PurePath] for the current working directory.
     */
    public actual fun cwd(): Path {
        TODO("not implemented")
    }

    /**
     * Creates a new [PurePath] for the current user's home directory.
     */
    public actual fun home(): Path {
        TODO("not implemented")
    }

    /**
     * Creates a new platform [Path] from the given [ByteString].
     */
    public actual fun path(of: ByteString): Path {
        TODO("not implemented")
    }

    /**
     * Creates a new platform Path from the given [PurePath].
     */
    public actual fun path(of: PlatformPurePath): Path {
        TODO("not implemented")
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
    actual fun makeTempDirectory(prefix: String): Path {
        TODO("not implemented")
    }

}
