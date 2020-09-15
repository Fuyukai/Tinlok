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
 * Enumerations of valid IP protocols.
 */
public expect enum class IPProtocol {
    /** Kernel's choice */
    IPPROTO_IP,
    /** ICMP protocol */
    IPPROTO_ICMP,
    /** TCP protocol */
    IPPROTO_TCP,
    /** UDP protocol */
    IPPROTO_UDP
}
