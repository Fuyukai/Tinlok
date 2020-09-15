/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

/**
 * Base class for connection information objects.
 */
public abstract class ConnectionInfo {
    /** The AF_ address family for this address. */
    public abstract val family: AddressFamily

    /** The SOCK_ socket kind for this connection info. */
    public abstract val type: SocketType

    /** The IPPROTO_ socket protocol for this address. */
    public abstract val protocol: IPProtocol

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ConnectionInfo

        if (family != other.family) return false
        if (type != other.type) return false
        if (protocol != other.protocol) return false

        return true
    }

    override fun hashCode(): Int {
        var result = family.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + protocol.hashCode()
        return result
    }
}
