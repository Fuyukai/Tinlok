/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)

package tf.lotte.tinlok.system

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import platform.posix.*

/**
 * Common bridge function for eventfd.
 */
internal expect fun __eventfd(count: UInt, flags: Int): Int

/**
 * Common bridge function for open() using a ByteVar.
 */
internal expect fun __open(path: CPointer<ByteVar>, oflag: Int): Int

/**
 * Common bridge function for open() using a ByteVar, and extra permissions.
 */
internal expect fun __open(path: CPointer<ByteVar>, oflag: Int, perms: Int): Int

/**
 * Common bridge function for stat() using a ByteVar.
 */
internal expect fun __stat(pathname: CPointer<ByteVar>, statbuf: CPointer<stat>): Int

/**
 * Common bridge function for lstat() using a ByteVar.
 */
internal expect fun __lstat(pathname: CPointer<ByteVar>, statbuf: CPointer<stat>): Int

/**
 * Common bridge function for access() using a ByteVar.
 */
internal expect fun __access(path: CPointer<ByteVar>, amode: Int): Int

/**
 * Common bridge function for opendir() using a ByteVar.
 */
internal expect fun __opendir(path: CPointer<ByteVar>): CPointer<DIR>?

/**
 * Common bridge function for mkdir() using a ByteVar.
 */
internal expect fun __mkdir(path: CPointer<ByteVar>, mode: mode_t): Int

/**
 * Common bridge function for rmdir() using a ByteVar.
 */
internal expect fun __rmdir(path: CPointer<ByteVar>): Int

/**
 * Common bridge function for unlink() using a ByteVar.
 */
internal expect fun __unlink(path: CPointer<ByteVar>): Int

/**
 * Common bridge function for realpath() using a ByteVar.
 */
internal expect fun __realpath(
    path: CPointer<ByteVar>,
    resolved_path: CPointer<ByteVar>,
): CPointer<ByteVar>?

/**
 * Common bridge function for readlink() using a ByteVar.
 */
internal expect fun __readlink(
    path: CPointer<ByteVar>,
    buf: CPointer<ByteVar>,
    bufsize: size_t,
): ssize_t

/**
 * Common bridge function for rename() using a ByteVar.
 */
internal expect fun __rename(old: CPointer<ByteVar>, new: CPointer<ByteVar>): Int

/**
 * Common bridge function for symlink() using a ByteVar.
 */
internal expect fun __symlink(path1: CPointer<ByteVar>, path2: CPointer<ByteVar>): Int
