/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.time

/**
 * Gets a monotonic timestamp. The reference point for this timestamp is completely undefined, so
 * it is only usable for duration timekeeping. This returns a nanosecond timestamp.
 */
public expect fun monotonicTimestamp(): Long
