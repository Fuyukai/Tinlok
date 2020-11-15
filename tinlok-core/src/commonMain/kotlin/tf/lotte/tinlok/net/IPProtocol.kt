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
 * Defines a single IP protocol for usage in socket creation.
 */
public interface IPProtocol {
    /** The socket number for the IP protocol. */
    public val number: Int
}

/**
 * Enumerations of valid IP protocols.
 */
public enum class StandardIPProtocols(override val number: Int) : IPProtocol {
    /** Kernel's choice */
    IPPROTO_IP(tf.lotte.tinlok.net.socket.IPPROTO_IP),

    /** ICMP protocol */
    IPPROTO_ICMP(tf.lotte.tinlok.net.socket.IPPROTO_ICMP),

    /** TCP protocol */
    IPPROTO_TCP(tf.lotte.tinlok.net.socket.IPPROTO_TCP),

    /** UDP protocol */
    IPPROTO_UDP(tf.lotte.tinlok.net.socket.IPPROTO_UDP);
}
