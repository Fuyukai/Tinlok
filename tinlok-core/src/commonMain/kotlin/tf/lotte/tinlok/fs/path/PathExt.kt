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
import tf.lotte.tinlok.fs.FilesystemFile
import tf.lotte.tinlok.fs.StandardOpenModes
import tf.lotte.tinlok.io.use
import tf.lotte.tinlok.toByteString
import tf.lotte.tinlok.util.Unsafe
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * Creates a new platform [Path] from the string [path].
 */
public fun Path.Companion.of(path: String): Path = of(path.toByteString())

/**
 * Joins this path to another [ByteString], returning the combined path.
 */
public fun Path.join(other: ByteString): Path = join(PlatformPaths.purePath(other))

/**
 * Joins this path to another String, returning the combined path.
 */
public fun Path.join(other: String): Path = join(other.toByteString())

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: PurePath): Path = join(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: String): Path = join(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: ByteString): Path = join(other)


// == Path I/O extensions == //
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

/**
 * Reads all of the bytes from the file represented by this Path.
 */
public fun Path.readAllBytes(): ByteString =
    open(StandardOpenModes.READ) {
        it.readAll()
    }

/**
 * Reads all of the bytes from the file represented by this Path, and decode it into a [String].
 */
public fun Path.readAllString(): String = readAllBytes().decode()

/**
 * Creates a new temporary directory and calls the provided lambda with its path. The
 * directory will be automatically deleted when the lambda returns.
 */
@OptIn(Unsafe::class, ExperimentalContracts::class)
public inline fun <R> Path.Companion.makeTempDirectory(prefix: String, block: (Path) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val dir = PlatformPaths.makeTempDirectory(prefix)
    try {
        return block(dir)
    } finally {
        dir.recursiveDelete()
    }
}
