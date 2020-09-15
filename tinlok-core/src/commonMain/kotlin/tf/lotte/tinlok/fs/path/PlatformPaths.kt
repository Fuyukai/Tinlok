/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.ByteString
import tf.lotte.tinlok.util.Unsafe

/**
 * Helper factory object for creating new [Path] and [PurePath] objects.
 */
public expect object PlatformPaths {
    /** The path separator that separates individual path components. */
    public val pathSeparator: ByteString

    /**
     * Creates a new [PurePath] for the current working directory.
     */
    public fun cwd(): Path

    /**
     * Creates a new [PurePath] for the current user's home directory.
     */
    public fun home(): Path

    /**
     * Creates a new platform [Path] from the given [ByteString].
     */
    public fun path(of: ByteString): Path

    /**
     * Creates a new platform Path from the given [PurePath].
     */
    public fun path(of: PurePath): Path

    /**
     * Creates a new platform [PurePath] from the given [ByteString].
     */
    public fun purePath(of: ByteString): PurePath

    /**
     * Creates a new temporary directory and returns its path.
     */
    @Unsafe
    public fun makeTempDirectory(prefix: String = "kotlin----"): Path
}
