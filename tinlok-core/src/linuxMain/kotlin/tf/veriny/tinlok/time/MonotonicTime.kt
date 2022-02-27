/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.time

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*
import tf.veriny.tinlok.system.Syscall

/**
 * Shared implementation of monotonic-ness.
 */
internal fun monotonicImpl(): Long = memScoped {
    val s = alloc<timespec>()
    val res = clock_gettime(CLOCK_MONOTONIC, s.ptr)
    if (res < 0) {
        Syscall.throwErrno(errno)
    }

    (s.tv_sec * 1_000_000_000L) + s.tv_sec
}
