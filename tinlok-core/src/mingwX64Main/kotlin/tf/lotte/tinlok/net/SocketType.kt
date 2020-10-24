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
public actual enum class SocketType(public actual val number: Int) {
    /** Stream sockets (TCP) */
    SOCK_STREAM(platform.windows.SOCK_STREAM),

    /** Datagram sockets (UDP) */
    SOCK_DGRAM(platform.windows.SOCK_DGRAM),

    /** Raw sockets (SCARY) */
    SOCK_RAW(platform.windows.SOCK_RAW),
    ;
    
}