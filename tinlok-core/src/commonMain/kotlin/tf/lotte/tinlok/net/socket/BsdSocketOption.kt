/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

/**
 * Interface defining a BSD socket option.
 */
public expect interface BsdSocketOption<T> : SocketOption<T> {
    /** The BSD option value, in int form. */
    public val bsdOptionValue: Int
}
