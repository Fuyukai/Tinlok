/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package tf.veriny.tinlok.system

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import platform.posix.*

internal actual fun __strlen(s: CPointer<ByteVar>): size_t {
    return platform.linux.extra.strlen(s)
}

internal actual fun __strnlen(s: CPointer<ByteVar>, max_size: size_t): size_t {
    return platform.linux.extra.strnlen(s, max_size)
}

internal fun __eventfd(count: UInt, flags: Int): Int {
    return platform.linux.extra.eventfd(count, flags)
}

internal fun __open(path: CPointer<ByteVar>, oflag: Int): Int {
    return platform.linux.extra.open(path, oflag)
}

internal fun __open(path: CPointer<ByteVar>, oflag: Int, perms: Int): Int {
    return platform.linux.extra.open(path, oflag, perms)
}

internal fun __stat(pathname: CPointer<ByteVar>, statbuf: CPointer<stat>): Int {
    return platform.linux.extra.stat(pathname, statbuf)
}

internal fun __lstat(pathname: CPointer<ByteVar>, statbuf: CPointer<stat>): Int {
    return platform.linux.extra.lstat(pathname, statbuf)
}

internal fun __access(path: CPointer<ByteVar>, amode: Int): Int {
    return platform.linux.extra.access(path, amode)
}

internal fun __opendir(path: CPointer<ByteVar>): CPointer<DIR>? {
    return platform.linux.extra.opendir(path)
}

internal fun __mkdir(path: CPointer<ByteVar>, mode: mode_t): Int {
    return platform.linux.extra.mkdir(path, mode)
}

internal fun __rmdir(path: CPointer<ByteVar>): Int {
    return platform.linux.extra.rmdir(path)
}

internal fun __unlink(path: CPointer<ByteVar>): Int {
    return platform.linux.extra.unlink(path)
}

internal fun __realpath(
    path: CPointer<ByteVar>,
    resolved_path: CPointer<ByteVar>,
): CPointer<ByteVar>? {
    return platform.linux.extra.realpath(path, resolved_path)
}

internal fun __readlink(
    path: CPointer<ByteVar>,
    buf: CPointer<ByteVar>,
    bufsize: size_t,
): ssize_t {
    return platform.linux.extra.readlink(path, buf, bufsize).convert()
}

internal fun __rename(old: CPointer<ByteVar>, new: CPointer<ByteVar>): Int {
    return platform.linux.extra.rename(old, new)
}

internal fun __symlink(path1: CPointer<ByteVar>, path2: CPointer<ByteVar>): Int {
    return platform.linux.extra.symlink(path1, path2)
}
