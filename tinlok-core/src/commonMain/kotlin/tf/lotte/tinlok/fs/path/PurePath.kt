/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.util.Unsafe

/**
 * A path to a file or folder on the filesystem. All PurePaths are immutable.
 *
 * Pure path objects provide path-handling operations which don’t actually access a filesystem.
 */
public interface PurePath {
    public companion object {
        /**
         * Creates a new [PurePath] corresponding to the current OS's path schema.
         */
        public fun native(path: ByteString): PlatformPurePath {
            return PlatformPaths.purePath(path)
        }

        /**
         * Creates a new [PosixPurePath].
         */
        public fun posix(path: ByteString): PosixPurePath {
            return PosixPurePath.fromByteString(path)
        }

        // TODO: Windows
    }

    /**
     * If this path is absolute (i.e. it doesn't need to be combined with another path to
     * represent a file).
     */
    public val isAbsolute: Boolean

    /** The raw components that comprise this path. */
    public val rawComponents: List<ByteString>

    /**
     * The string representation of the components that comprise this path.
     */
    public val components: List<String>

    /**
     * The raw anchor of this path.
     */
    public val rawAnchor: ByteString?

    /**
     * The anchor of this path.
     */
    public val anchor: String?

    /** The parent of this Path. */
    public val parent: PurePath

    /** The raw final name of this Path. */
    public val rawName: ByteString

    /** The final name of this path. */
    public val name: String

    /**
     * Resolves the other path as if it was a child of this path.
     *
     * If the other path is absolute, it will simply replace this path. Otherwise, it will be
     * concatenated onto the end of this path.
     */
    public fun resolveChild(other: PurePath): PurePath

    /**
     * Replaces the name of this path, returning the new path.
     */
    public fun withName(name: ByteString): PurePath

    /**
     * Checks if this path is the child of the [other] path.
     */
    public fun isChildOf(other: PurePath): Boolean

    /**
     * Changes the parent of a path from the parent [from] to the new parent [to].
     *
     * For example, reparenting ``/usr/lib/python3.8/site-packages`` from ``/usr`` to
     * ``/usr/local`` would return ``/usr/local/lib/python3.8/site-packages``.
     */
    public fun reparent(from: PurePath, to: PurePath): PurePath

    /**
     * Converts the path within to a Kotlin string. This *will* break if non-unicode paths are used.
     */
    @Unsafe
    public fun unsafeToString(): String
}
