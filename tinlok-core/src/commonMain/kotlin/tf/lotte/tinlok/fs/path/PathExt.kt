/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.fs.DirEntry
import tf.lotte.tinlok.fs.StandardOpenModes
import tf.lotte.tinlok.fs.SynchronousFile
import tf.lotte.tinlok.io.*
import tf.lotte.tinlok.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private val TEMP_SUFFIX = b(".~")

/**
 * Creates a new platform [Path] from the string [path].
 */
public fun Path.Companion.of(path: String): Path = of(path.toByteString())

/**
 * Joins this path to another [ByteString], returning the combined path.
 */
public fun Path.resolveChild(other: ByteString): Path = resolveChild(PlatformPaths.purePath(other))

/**
 * Joins this path to another String, returning the combined path.
 */
public fun Path.resolveChild(other: String): Path = resolveChild(other.toByteString())

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: PurePath): Path = resolveChild(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: String): Path = resolveChild(other)

/**
 * Helper operator function for fluent API usage.
 */
public operator fun Path.div(other: ByteString): Path = resolveChild(other)

/**
 * Replaces the name of this path, returning the new path.
 */
public fun Path.withName(name: String): Path = withName(name.toByteString())


// TODO: Maybe move this to scandir.
// == Path I/O extensions == //
/**
 * Flattens a Path tree into a listing of [DirEntry].
 *
 * This will be ordered in most components to least (so mostly deeply nested to least).
 */
public fun Path.flattenTree(): List<DirEntry> {
    // this descends into every directory without being recursive in itself.
    // this avoids death by stack overflow
    val final = mutableListOf<DirEntry>()
    val pending = ArrayDeque(listDir())

    // examine all items in pending. if it's a directory, add all child items onto pending
    // if it's not, add it to final
    // then on the next pass around pending those subdirectories are recursively examined
    for (item in pending) {
        if (item.isDirectory(followSymlinks = false)) {
            for (subitem in item.path.listDir()) {
                pending.add(subitem)
            }
            final.add(item)
        } else {
            final.add(item)
        }
    }

    return final.sortedByDescending { it.path.rawComponents.size }
}

/**
 * Recursively deletes a directory.
 */
public fun Path.recursiveDelete() {
    val flattened = flattenTree()

    // remove all entries in the tree from most deeply nested to least deeply nested
    for (i in flattened) {
        try {
            if (!i.isDirectory(followSymlinks = false)) {
                i.path.unlink()
            } else {
                i.path.removeDirectory()
            }
        } catch (e: FileNotFoundException) {
            // who cares
            // usually because we tried to remove a file inside a symlink'd directory after the
            // original file
        }
    }

    this.removeDirectory()
}

// XXX: This is not particularly efficient allocation wise...
//      I'm justifying it by this being an uncommon operation.
//      Somebody can feel free to optimise this.
/**
 * Recursively copies a directory to another path.
 */
@OptIn(Unsafe::class)
public fun Path.recursiveCopy(to: Path) {
    // TODO: This can error falsely if the directory is deleted between the exists() and
    //  isDirectory check.

    val fromAbsolute = this.resolveFully(strict = true)
    // this is reversed because the paths are in nested to unnested order, whereas we need
    // to make them in the opposite order
    val allPaths = fromAbsolute.flattenTree().asReversed()

    if (to.exists()) {
        if (!to.isDirectory(followSymlinks = true)) {
            throw FileAlreadyExistsException(to.toByteString())
        }
    } else {
        to.createDirectory(parents = true, existOk = false)
    }


    for (old in allPaths) {
        val new = old.path.reparent(fromAbsolute, to)
        val linkTarget = old.path.linkTarget()
        when {
            // simply make a new symlink
            linkTarget != null -> new.symlinkTo(linkTarget)
            // new empty directory
            old.isDirectory(followSymlinks = false) -> {
                new.createDirectory(parents = false, existOk = true)
            }
            // just a file, copy it
            old.isRegularFile(followSymlinks = false) -> {
                old.path.copyFile(new)
            }
        }
    }
}

// == Combination helpers == //

/**
 * Moves any file or directory safely. This is the recommended method to move a path safely as it
 * will work cross-filesystem.
 *
 * This method is not guaranteed to be atomic but the new file or directory will exist before the
 * old file or directory is removed if a non-atomic method is chosen.
 */
@OptIn(Unsafe::class)
public fun Path.move(to: Path) {
    if (isSafeToRename(to)) {
        this.rename(to)
    } else {
        // copy then delete
        this.copy(to)
        this.delete()
    }
}

/**
 * Copies the file or folder at this path to the specified other path. This is recommended over
 * using copyFile or recursiveCopy as it will pick the right method appropriately.
 */
@OptIn(Unsafe::class)
public fun Path.copy(to: Path) {
    val link = this.linkTarget()
    if (link != null) {
        return to.symlinkTo(link)
    }

    // not a symlink..
    // maybe a directory?
    if (isDirectory(followSymlinks = false)) {
        return recursiveCopy(to)
    }

    // not a directory either...
    // just copy it as a file.
    copyFile(to)
}


/**
 * Helper function that deletes the directory or file at this path. This is recommended
 * over using recursiveDelete or unlink as it will pick the right method appropriately.
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
public inline fun <R> Path.open(vararg modes: StandardOpenModes, block: (SynchronousFile) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return unsafeOpen(*modes).use(block)
}

/**
 * Opens a path for file I/O, adding it to the specified [scope] for automatic closing.
 */
@OptIn(Unsafe::class)
public fun Path.open(scope: ClosingScope, vararg modes: StandardOpenModes): SynchronousFile {
    val file = unsafeOpen(*modes)
    scope.add(file)
    return file
}

/**
 * Writes the specified [ByteString] [bs] to the file represented by this [Path]. The file will be
 * created if it doesn't exist.
 *
 * If [atomic] is true, the file will be created atomically; either all of the content will be
 * written or none will. (This is achieved with a write to a temporary file, then renaming the
 * temporary file over the real file.)
 */
@OptIn(Unsafe::class)
public fun Path.writeBytes(bs: ByteString, atomic: Boolean = true) {
    if (rawName == null) {
        throw IllegalArgumentException("Cannot write to a path with no name!")
    }

    if (atomic) {
        val realPath = if (this.exists()) {
            if (this.isDirectory(followSymlinks = true)) {
                throw IsADirectoryException(toByteString())
            }

            if (!this.isRegularFile(followSymlinks = false) && !this.isLink()) {
                throw FileAlreadyExistsException(toByteString())
            }

            // resolve through symlinks
            this.resolveFully(strict = true)
        } else this

        val tempName = realPath.withName(rawName!! + TEMP_SUFFIX)

        tempName.open(StandardOpenModes.WRITE, StandardOpenModes.CREATE) {
            it.writeAll(bs)
        }

        tempName.move(realPath)
    } else {
        open(StandardOpenModes.WRITE, StandardOpenModes.CREATE) {
            it.writeAll(bs)
        }
    }
}

/**
 * Writes the specified [String] [str] to the file represented by this [Path]. The file will be
 * created if it doesn't exist.
 *
 * If [atomic] is true, the file will be created atomically; either all of the content will be
 * written or none will. (This is achieved with a write to a temporary file, then renaming the
 * temporary file over the real file.)
 */
public fun Path.writeString(str: String, atomic: Boolean = true) {
    writeBytes(str.toByteString(), atomic = atomic)
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
public fun Path.readAllText(): String = readAllBytes().decode()

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
    return AutodeletePath(dir).use(block)
}

/**
 * Creates a new temporary directory,
 */
@OptIn(Unsafe::class)
public fun Path.Companion.makeTempDirectory(scope: ClosingScope, prefix: String): Path {
    val dir = PlatformPaths.makeTempDirectory(prefix)
    val adir = AutodeletePath(dir)
    scope.add(adir)
    return adir
}
