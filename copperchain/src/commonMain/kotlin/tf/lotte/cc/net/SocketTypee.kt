/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.net

/**
 * Defines a single socket type for usage in socket creation.
 */
public interface SocketType {
    /** The socket number for the socket type. */
    public val number: Int
}

/**
 * An enumeration of the available socket kinds.
 */
public enum class StandardSocketTypes(override val number: Int) : SocketType {
    /** Stream sockets (TCP) */
    SOCK_STREAM(1),

    /** Datagram sockets (UDP) */
    SOCK_DGRAM(2),

    /** Raw sockets (SCARY) */
    SOCK_RAW(3),
    ;
}
