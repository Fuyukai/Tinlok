/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net

/**
 * An enumeration of the available address families.
 */
public enum class AddressFamily(public val number: Int) {
    // main IP
    /** Unspecified address family. */
    AF_UNSPEC(tf.veriny.tinlok.net.socket.AF_UNSPEC),

    /** IPv4 */
    AF_INET(tf.veriny.tinlok.net.socket.AF_INET),

    /** IPv6 */
    AF_INET6(tf.veriny.tinlok.net.socket.AF_INET6),
    // AF_UNIX is available on newer versions of Windows 10 so it's available on all platforms
    /** Unix pipes */
    AF_UNIX(tf.veriny.tinlok.net.socket.AF_UNIX),
    ;
}
