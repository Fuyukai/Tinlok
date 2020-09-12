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
 * Abstract base class for all socket addresses that operate over the Internet Protocol (IP).
 */
public abstract class InetSocketAddress(
    /** The IP address of this socket address. */
    public val ip: IPAddress
) : SocketAddress() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as InetSocketAddress

        if (ip != other.ip) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ip.hashCode()
        return result
    }
}
