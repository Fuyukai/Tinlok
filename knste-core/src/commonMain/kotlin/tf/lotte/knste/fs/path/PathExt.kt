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
import tf.lotte.knste.fs.FilesystemFile
import tf.lotte.knste.fs.StandardOpenModes
import tf.lotte.knste.io.use
import tf.lotte.knste.toByteString
import tf.lotte.knste.util.Unsafe
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates a new temporary directory and calls the provided lambda with its path. The
 * directory will be automatically deleted when the lambda returns.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public fun <R> Paths.makeTempDirectory(prefix: String, block: (Path) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val dir = makeTempDirectory(prefix)
    try {
        return block(dir)
    } finally {
        dir.recursiveDelete()
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
    // then on the next pass around pending those subdirectories are recursively examined
    for (item in pending) {
        if (item.isDirectory(followSymlinks = false)) {
            for (subitem in item.listDir()) {
                pending.add(subitem)
            }
            final.add(item)
        } else {
            final.add(item)
        }
    }

    return final.sortedByDescending { it.rawComponents.size }
}

/**
 * Recursively deletes a directory.
 */
public fun Path.recursiveDelete() {
    val flattened = flattenTree()

    // remove all entries in the tree from most deeply nested to least deeply nested
    for (i in flattened) {
        if (!i.isDirectory(followSymlinks = false)) {
            i.unlink()
        } else {
            i.removeDirectory()
        }
    }

    this.removeDirectory()
}

/**
 * Opens a path for file I/O, calling the specified lambda with the opened file. The file will be
 * automatically closed when the lambda exits.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R> Path.open(vararg modes: StandardOpenModes, block: (FilesystemFile) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return unsafeOpen(*modes).use(block)
}

/**
 * Writes the specified [ByteString] [bs] to the file represented by this [Path]. The file will be
 * created if it doesn't exist.
 */
public fun Path.writeBytes(bs: ByteString) {
    open(StandardOpenModes.WRITE, StandardOpenModes.CREATE) {
        it.writeAll(bs)
    }
}

/**
 * Writes the specified [String] [str] to the file represented by this [Path]. The file will be
 * created if it doesn't exist.
 */
public fun Path.writeString(str: String) {
    writeBytes(str.toByteString())
}
