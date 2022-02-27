/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.system

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import platform.posix.size_t

/**
 * Exposes the fast libc strlen, for usage instead of slow Kotlin methods.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect fun __strlen(s: CPointer<ByteVar>): size_t

/**
 * Exposes the fast libc strnlen, for usage instead of slow Kotlin methods.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect fun __strnlen(s: CPointer<ByteVar>, max_size: size_t): size_t
