/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

// see: https://docs.python.org/3/library/ipaddress.html
// see: https://doc.rust-lang.org/std/net/enum.IpAddr.html
// and of course, JDK InetAddress

/**
 * Abstract sealed superclass for all IP addresses.
 */
public sealed class IPAddress {
    public companion object {
        public const val IP_VERSION_4: Int = 4
        public const val IP_VERSION_6: Int = 6
    }

    /** The version number for this address (e.g. 4 for IPv4, 6 for IPv6) */
    public abstract val version: Int

    /** The address family for this IP address. */
    public abstract val family: AddressFamily

    // TODO: Other attributes that other languages have but aren't needed for a prototype right now.
}

/**
 * An IP address using version 4.
 */
public class IPv4Address
private constructor(private val rawRepresentation: UInt) : IPAddress() {
    override val version: Int = IP_VERSION_4
    override val family: AddressFamily get() = AddressFamily.AF_INET
}

public class IPv6Address
private constructor(private val lower: ULong, private val upper: ULong) : IPAddress() {
    override val version: Int = IP_VERSION_6
    override val family: AddressFamily get() = AddressFamily.AF_INET6
}
