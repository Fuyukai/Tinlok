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
 * Defines a single address family for usage in socket creation.
 */
public interface AddressFamily {
    /** The socket number for the address family. */
    public val number: Int
}

/**
 * An enumeration of the available address families.
 */
public enum class StandardAddressFamilies(override val number: Int) : AddressFamily {
    // main IP
    /** Unspecified address family. */
    AF_UNSPEC(0),

    /** IPv4 */
    AF_INET(2),

    /** IPv6 */
    AF_INET6(10),
    // AF_UNIX is available on newer versions of Windows 10 so it's available on all platforms
    /** Unix pipes */
    AF_UNIX(1),
    ;

}