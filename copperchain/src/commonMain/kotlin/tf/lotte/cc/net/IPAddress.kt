/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.net

import tf.lotte.cc.Unsafe
import tf.lotte.cc.util.toByteArray

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
@OptIn(ExperimentalUnsignedTypes::class)
public class IPv4Address(public val bytes: ByteArray) : IPAddress() {
    public companion object {
        /**
         * Parses an IPv4 address from a String.
         */
        public fun of(ip: String): IPv4Address {
            val split = ip.split('.')
            require(split.size == 4) { "Invalid IPv4 address: $ip" }
            require(split.all { it.isNotEmpty() }) { "Invalid IPv4 address: $ip" }

            val ints = split.map { it.toInt() }
            require(!ints.any { it > 255 }) { "Invalid IPv4 address: $ip" }
            return IPv4Address(ints.map { it.toByte() }.toByteArray())
        }

        /**
         * Parses an IPv4 address from a decimal [UInt].
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        public fun of(decimal: UInt): IPv4Address {
            return IPv4Address(decimal.toByteArray())
        }
    }

    override val version: Int = IP_VERSION_4
    override val family: AddressFamily get() = StandardAddressFamilies.AF_INET

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IPv4Address

        if (!bytes.contentEquals(other.bytes)) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + version
        return result
    }

    override fun toString(): String {
        return bytes.joinToString(".") { it.toUByte().toString() }
    }
}

/**
 * An IP address using version 6.
 */
public class IPv6Address(public val bytes: ByteArray) : IPAddress() {
    public companion object {
        /**
         * Parses an IPv6 address from a String.
         */
        public fun of(ip: String): IPv6Address {
            val bytes = IPv6TextParser.parse(ip)
            return IPv6Address(bytes)
        }
    }

    override val version: Int = IP_VERSION_6
    override val family: AddressFamily get() = StandardAddressFamilies.AF_INET6

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as IPv6Address

        if (!bytes.contentEquals(other.bytes)) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + version
        return result
    }

    @OptIn(Unsafe::class)
    override fun toString(): String {
        return IPv6Stringifier(bytes).correct()
    }
}