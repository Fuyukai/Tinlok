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

/**
 * A path to a file or folder on the filesystem. All PurePath (and instances) are immutable.
 *
 * Pure path objects provide path-handling operations which don’t actually access a filesystem.
 */
public interface PurePath {
    /**
     * If this path is absolute (i.e. it doesn't need to be combined with another path to
     * represent a file).
     */
    public val isAbsolute: Boolean

    /** The raw components that comprise this path. */
    public val rawComponents: List<ByteString>

    /**
     * The string representation of the components that comprise this path.
     *
     * This will decode the underlying components on every read
     */
    public val components: List<String>

    /** The parent of this Path. */
    public val parent: PurePath

    /** The raw final name of this Path. */
    public val rawName: ByteString

    /** The final name of this path. */
    public val name: String

    /**
     * Joins this path to another path, returning the combined path.
     *
     * If the other path is absolute, it will simply replace this path. Otherwise, it will be
     * concatenated onto the end of this path.
     */
    public fun join(other: PurePath): PurePath

    /**
     * Joins this path to another [ByteString], returning the combined path.
     */
    public fun join(other: ByteString): PurePath

    /**
     * Converts the path within to a Kotlin string. This *will* break if non-unicode paths are used.
     */
    public fun extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails(): String
}

