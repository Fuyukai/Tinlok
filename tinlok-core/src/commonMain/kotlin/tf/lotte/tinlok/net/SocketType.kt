/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
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
public enum class SocketType(public val number: Int) {
    /** Stream sockets (TCP) */
    SOCK_STREAM(tf.lotte.tinlok.net.socket.SOCK_STREAM),

    /** Datagram sockets (UDP) */
    SOCK_DGRAM(tf.lotte.tinlok.net.socket.SOCK_DGRAM),

    /** Raw sockets (SCARY) */
    SOCK_RAW(tf.lotte.tinlok.net.socket.SOCK_RAW),
    ;
}
