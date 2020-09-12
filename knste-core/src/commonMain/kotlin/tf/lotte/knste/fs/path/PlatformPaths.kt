/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import tf.lotte.knste.ByteString
import tf.lotte.knste.util.Unsafe

/**
 * Helper factory object for creating new [Path] and [PurePath] objects.
 */
@PublishedApi
internal expect object PlatformPaths {
    /** The path separator that separates individual path components. */
    val pathSeparator: ByteString

    /**
     * Creates a new [PurePath] for the current working directory.
     */
    fun cwd(): Path

    /**
     * Creates a new [PurePath] for the current user's home directory.
     */
    fun home(): Path

    /**
     * Creates a new platform [Path] from the given [ByteString].
     */
    fun path(of: ByteString): Path

    /**
     * Creates a new platform Path from the given [PurePath].
     */
    fun path(of: PurePath): Path

    /**
     * Creates a new platform [PurePath] from the given [ByteString].
     */
    fun purePath(of: ByteString): PurePath

    /**
     * Creates a new temporary directory and returns its path.
     */
    @Unsafe
    @PublishedApi
    internal fun makeTempDirectory(prefix: String = "kotlin----"): Path
}
