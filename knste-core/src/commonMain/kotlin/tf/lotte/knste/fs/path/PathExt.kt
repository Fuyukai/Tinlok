/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:JvmName("PathExt")
package tf.lotte.knste.fs.path

import kotlin.jvm.JvmName

/**
 * Creates a new temporary directory and calls the provided lambda with its path. The
 * directory will be automatically deleted when the lambda returns.
 */
public fun <R> Paths.makeTempDirectory(block: (Path) -> R): R {
    val dir = unsafeMakeTempDirectory()
    try {
        dir.createDirectory(parents = false)
        return block(dir)
    } finally {
        TODO("recursive deletion")
    }
}

public operator fun PurePath.div(other: PurePath): PurePath = join(other)

/**
 * Recursively gets all of the parents of this Path.
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

