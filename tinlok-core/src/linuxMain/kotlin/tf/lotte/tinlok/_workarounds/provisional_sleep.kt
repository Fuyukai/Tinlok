/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok._workarounds

/**
 * Provisional sleep
 */
public actual fun provisional_sleep(millis: Long) {
    platform.posix.usleep(millis.toUInt() * 1000U)
}
