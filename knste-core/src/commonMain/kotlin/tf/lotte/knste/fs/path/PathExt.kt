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

import tf.lotte.knste.util.Unsafe
import kotlin.jvm.JvmName

/**
 * Creates a new temporary directory and calls the provided lambda with its path. The
 * directory will be automatically deleted when the lambda returns.
 */
@OptIn(Unsafe::class)
public fun <R> Paths.makeTempDirectory(block: (Path) -> R): R {
    val dir = makeTempDirectory()
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

/**
 * Flattens a Path tree into a listing of [Path].
 *
 * This will be ordered in most components to least (so mostly deeply nested to least).
 */
public fun Path.flattenTree(): List<Path> {
    // this descends into every directory without being recursive in itself.
    // this avoids death by stack overflow
    val final = mutableListOf<Path>()
    val pending = ArrayDeque(listDir())

    // examine all items in pending. if it's a directory, add all child items onto pending
    // if it's not, add it to final
    // then on the next pass around pending
    val it = pending.listIterator()
    for (item in it) {
        if (item.isDirectory(followSymlinks = false)) {
            item.listDir().forEach(it::add)
            final.add(item)
        } else {
            final.add(item)
        }
    }

    return final.sortedBy { it.rawComponents.size }
}

/**
 * Recursively deletes a directory.
 */
public fun Path.recursiveDelete() {
    for (i in flattenTree()) {
        if (!i.isDirectory(followSymlinks = false)) {
            i.removeDirectory()
        } else {
            i.unlink()
        }
    }
}
