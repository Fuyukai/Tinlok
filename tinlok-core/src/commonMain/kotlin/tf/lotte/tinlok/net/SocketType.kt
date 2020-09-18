/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

/**
 * An enumeration of the available socket kinds.
 */
public expect enum class SocketType {
    /** Stream sockets (TCP) */
    SOCK_STREAM,
    /** Datagram sockets (UDP) */
    SOCK_DGRAM,
    /** Raw sockets (SCARY) */
    SOCK_RAW,
    ;

    /** The number of this socket kind. */
    public val number: Int
}
