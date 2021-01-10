/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.time

import kotlinx.cinterop.*
import platform.windows.LARGE_INTEGER
import platform.windows.QueryPerformanceCounter
import tf.veriny.tinlok.Unsafe

@ThreadLocal
private var LAST_NOW = 0L

/**
 * Gets a monotonic timestamp. The reference point for this timestamp is completely undefined, so
 * it is only usable for duration timekeeping.
 */
@OptIn(Unsafe::class)
public actual fun monotonicTimestamp(): Long = memScoped {
    // According to Rust, QPC on X86_64 isn't always monotonic. Uh oh!
    // Instead, we cache a good value, and return it if QPC returns a value in the past.
    val lvar = alloc<LongVar>()
    val res = QueryPerformanceCounter(lvar.reinterpret<LARGE_INTEGER>().ptr)
    if (LAST_NOW > lvar.value) {
        return LAST_NOW
    }
    LAST_NOW = lvar.value
    return LAST_NOW
}
