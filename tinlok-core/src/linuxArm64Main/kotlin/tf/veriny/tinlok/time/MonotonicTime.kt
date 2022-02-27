/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.time

private var LAST_NOW = 0L

/**
 * Gets a monotonic timestamp. The reference point for this timestamp is completely undefined, so
 * it is only usable for duration timekeeping. This returns a nanosecond timestamp.
 */
public actual fun monotonicTimestamp(): Long {
    // According to rust, clock_gettime(CLOCK_MONOTONIC, ...) isn't monotonic on AArch64.
    // So we cache a good value, and return it if clock_gettime is in the past.
    val value = monotonicImpl()
    if (LAST_NOW > value) {
        return LAST_NOW
    }
    LAST_NOW = value
    return LAST_NOW
}
