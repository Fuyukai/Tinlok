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
 * A socket address for TCP sockets.
 */
public class TcpSocketAddress(
    ip: IPAddress,
    /** The port of this socket address. */
    public val port: Int
) : SocketAddress(ip) {
    override val family: AddressFamily get() = ipAddress.family
    override val kind: SocketKind get() = SocketKind.SOCK_STREAM
    override val protocol: IPProtocol get() = IPProtocol.IPPROTO_TCP


    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + port
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is TcpSocketAddress) return false
        if (!super.equals(other)) return false
        if (port != other.port) return false

        return true
    }


}
