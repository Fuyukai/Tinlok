/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

public actual enum class IPProtocol(public val number: Int) {
    /** Kernel's choice */
    IPPROTO_IP(platform.posix.IPPROTO_IP),

    /** ICMP protocol */
    IPPROTO_ICMP(platform.posix.IPPROTO_ICMP),

    /** TCP protocol */
    IPPROTO_TCP(platform.posix.IPPROTO_TCP),

    /** UDP protocol */
    IPPROTO_UDP(platform.posix.IPPROTO_UDP)
}
