/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

/**
 * Base abstract class for all socket addresses.
 */
public abstract class SocketAddress {
    /** The AF_ address family for this address. */
    public abstract val family: AddressFamily

    /** The SOCK_ socket kind for this address. */
    public abstract val kind: SocketKind

    /** The IPPROTO_ socket protocol for this address. */
    public abstract val protocol: IPProtocol

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is SocketAddress) return false
        if (family != other.family) return false
        if (kind != other.kind) return false
        if (protocol != other.protocol) return false

        return true
    }

    override fun hashCode(): Int {
        var result = family.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + protocol.hashCode()
        return result
    }
}

/**
 * Gets the port of a socket address.
 */
public val SocketAddress.port: Int get() {
    return when (this) {
        is TcpSocketAddress -> port
        is DatagramSocketAddress -> port
        else -> 0
    }
}
