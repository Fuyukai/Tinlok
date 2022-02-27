/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net

/**
 * Abstract base class for all connection info classes that operate over the Internet Protocol (IP).
 */
public abstract class InetConnectionInfo(
    /** The IP address of this socket address. */
    public val ip: IPAddress,
    /** The port of this info, or 0 if this doesn't have a port. */
    public val port: Int,
) : ConnectionInfo() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as InetConnectionInfo

        if (ip != other.ip) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ip.hashCode()
        return result
    }
}
