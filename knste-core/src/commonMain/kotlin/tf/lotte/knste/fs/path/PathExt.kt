/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import tf.lotte.knste.*
import tf.lotte.knste.fs.FilesystemFile
import tf.lotte.knste.fs.StandardOpenModes
import tf.lotte.knste.io.use
import tf.lotte.knste.util.Unsafe
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// == Paths extensions == //
/**
 * Creates a new temporary directory and calls the provided lambda with its path. The
 * directory will be automatically deleted when the lambda returns.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R> Paths.makeTempDirectory(prefix: String, block: (Path) -> R): R {
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

// == PurePath extensions == //
/**
 * Helper operator function for fluent API usage.
 */
public operator fun PurePath.div(other: PurePath): PurePath = join(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: PurePath): Path = join(other)

/**
 * Joins this pure path to another [ByteString], returning the combined path.
 */
public fun PurePath.join(other: ByteString): PurePath = join(Paths.purePath(other))

/**
 * Joins this path to another [ByteString], returning the combined path.
 */
public fun Path.join(other: ByteString): Path = join(Paths.purePath(other))

/**
 * Helper operator function for fluent API usage.
 */
public operator fun PurePath.div(other: ByteString): PurePath = join(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: ByteString): Path = join(other)

/**
 * Joins this path to another String, returning the combined path.
 */
public fun PurePath.join(other: String): PurePath = join(other.toByteString())

/**
 * Joins this path to another String, returning the combined path.
 */
public fun Path.join(other: String): Path = join(other.toByteString())

/**
 * Helper operator function for fluent API usage.
 */
public operator fun PurePath.div(other: String): PurePath = join(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: String): Path = join(other)

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

// == Path extensions == //
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

// todo cache the stat usages in flattenTree maybe?
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
 * Helper function that deletes a directory or a file with the right call.
 */
public fun Path.delete() {
    if (isDirectory(followSymlinks = false)) recursiveDelete()
    else unlink()
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
