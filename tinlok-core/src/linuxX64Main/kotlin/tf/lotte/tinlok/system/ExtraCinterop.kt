/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

internal actual fun __eventfd(count: UInt, flags: Int): Int {
    return platform.linux.extra.eventfd(count, flags)
}
