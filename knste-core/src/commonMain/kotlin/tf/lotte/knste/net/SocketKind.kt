/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

// TODO: see if we care about RDM/SEQPACKET

/**
 * An enumeration of the available socket kinds.
 */
public expect enum class SocketKind {
    /** Stream sockets (TCP) */
    SOCK_STREAM,
    /** Datagram sockets (UDP) */
    SOCK_DGRAM,
    /** Raw sockets (SCARY) */
    SOCK_RAW,
    /** Reliable datagrams, unimplemented (?) */
    SOCK_RDM,
    /** Sequential datagrams, unimplemented (?) */
    SOCK_SEQPACKET,

    ;

    /** The number of this socket kind. */
    public val number: Int
}