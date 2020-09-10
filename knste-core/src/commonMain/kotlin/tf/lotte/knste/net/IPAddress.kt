/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

import tf.lotte.knste.types.bytestring.ByteStringHolder

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

// both of these classes contain their IP address in network order bytearrays
/**
 * An IP address using version 4.
 */
public class IPv4Address
internal constructor(private val rawRepresentation: ByteArray) : IPAddress() {
    override val version: Int = IP_VERSION_4
    override val family: AddressFamily get() = AddressFamily.AF_INET

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IPv4Address

        if (!rawRepresentation.contentEquals(other.rawRepresentation)) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawRepresentation.contentHashCode()
        result = 31 * result + version
        return result
    }

    override fun toString(): String {
        return rawRepresentation.joinToString(".") { it.toString() }
    }
}

public class IPv6Address
internal constructor(private val rawRepresentation: ByteArray) : IPAddress() {
    override val version: Int = IP_VERSION_6
    override val family: AddressFamily get() = AddressFamily.AF_INET6

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IPv6Address

        if (!rawRepresentation.contentEquals(other.rawRepresentation)) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawRepresentation.contentHashCode()
        result = 31 * result + version
        return result
    }
}
