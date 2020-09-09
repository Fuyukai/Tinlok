/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

// TODO: Consider if we really care about stuff like AF_IPX, AF_SNA, AF_APPLETALK
// and all the other like 1970s protocols
// (although tbf BSD sockets/WinSock is 1970s cruft which is why we paper over it)

/**
 * An enumeration of the available address families.
 */
public expect enum class AddressFamily {
    // main IP
    /** Unspecified address family. */
    AF_UNSPEC,
    /** IPv4 */
    AF_INET,
    /** IPv6 */
    AF_INET6,
    // apparently these three are available on all three platforms
    /** IPX networking (?), I don't think anyone uses this */
    AF_IPX,
    /** Systems Network Architecture, I don't think anyone uses this */
    AF_SNA,
    /** AppleTalk */
    AF_APPLETALK,
    // AF_UNIX is available on newer versions of Windows 10 so it's available on all platforms
    /** Unix pipes */
    AF_UNIX,
    ;

    /** The socket number of this address family. */
    public val number: Int
}
