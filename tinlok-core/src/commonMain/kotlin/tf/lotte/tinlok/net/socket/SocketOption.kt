/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

/**
 * A socket option that can be set on a socket.
 *
 * @param T: The argument type for this option.
 */
public interface SocketOption<T> {
    /** The name of this socket option. */
    public val name: String

    /** The level of this socket option. */
    public val level: Int
}
