/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

/**
 * Linux enumeration of socket kinds.
 */
public actual enum class SocketKind(public actual val number: Int) {
    SOCK_STREAM(platform.posix.SOCK_STREAM),
    SOCK_DGRAM(platform.posix.SOCK_DGRAM),
    SOCK_RAW(platform.posix.SOCK_RAW),
    SOCK_RDM(platform.posix.SOCK_RDM),
    SOCK_SEQPACKET(platform.posix.SOCK_SEQPACKET),

    ;
}
