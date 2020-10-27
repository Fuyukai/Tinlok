/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok._workarounds

import platform.windows.Sleep

/**
 * Provisional sleep
 */
public actual fun provisional_sleep(millis: Long) {
    Sleep(millis.toUInt())
}