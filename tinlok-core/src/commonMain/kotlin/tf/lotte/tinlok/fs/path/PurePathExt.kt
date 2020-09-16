/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.*

// == Helper creators == //
/**
 * Creates a new [PurePath] corresponding to the current OS's path schema.
 */
public fun PurePath.Companion.native(path: String): PlatformPurePath = native(path.toByteString())

/**
 * Creates a new [PosixPurePath].
 */
public fun PurePath.Companion.posix(path: String): PosixPurePath = posix(path.toByteString())

/**
 * Joins this pure path to another [ByteString], returning the combined path.
 */
public fun PurePath.resolveChild(other: ByteString): PurePath = resolveChild(PlatformPaths.purePath(other))

/**
 * Joins this path to another String, returning the combined path.
 */
public fun PurePath.resolveChild(other: String): PurePath = resolveChild(other.toByteString())

/**
 * Helper operator function for fluent API usage.
 */
public operator fun PurePath.div(other: PurePath): PurePath = resolveChild(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun PurePath.div(other: ByteString): PurePath = resolveChild(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun PurePath.div(other: String): PurePath = resolveChild(other)

/**
 * Replaces the name of this path, returning the new path.
 */
public fun PurePath.withName(name: String): PurePath = withName(name.toByteString())


// == PurePath extensions == //

/**
 * Gets all of the parents of this Path.
 */
public fun PurePath.allParents(): List<PurePath> {
    // no parent
    return when (rawComponents.size) {
        1 -> listOf()
        // only one parent
        2 -> listOf(parent)
        // lots of parents
        else -> {
            val working = ArrayList<PurePath>(rawComponents.size - 1)
            var last = parent
            while (last.parent != last) {
                working.add(last)
                last = last.parent
            }
            working
        }
    }
}

// https://docs.python.org/3/library/pathlib.html#pathlib.PurePath.suffix
/**
 * Gets the file extension of the name of this file, if any. (``file.tar.gz`` -> ``gz``)
 */
public val PurePath.rawSuffix: ByteString?
    get() {
        val idx = rawName.lastIndexOf('.'.toByte())
        return if (idx <= -1) null
        else rawName.substring(idx + 1)
    }

/**
 * Gets the file extension of the name of this file, if any. (``file.tar.gz`` -> ``gz``)
 */
public val PurePath.suffix: String?
    get() {
        return rawSuffix?.decode()
    }

// https://docs.python.org/3/library/pathlib.html#pathlib.PurePath.suffixes
/**
 * Gets all the file extensions of the name of this file, if any.
 */
public val PurePath.rawSuffixes: List<ByteString>
    get() {
        if (!rawName.contains('.'.toByte())) return emptyList()
        val split = rawName.split(b("."))
        return split.drop(1)
    }

/**
 * Gets all the file extensions of the name of this file, if any.
 */
public val PurePath.suffixes: List<String>
    get() {
        // copy to avoid repeated decoding on default impls
        val nam = name

        if (!nam.contains('.')) return emptyList()
        val split = nam.split('.')
        return split.drop(1)
    }

