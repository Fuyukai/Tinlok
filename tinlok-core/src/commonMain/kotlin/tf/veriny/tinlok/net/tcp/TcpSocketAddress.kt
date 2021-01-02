/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.tcp

import tf.veriny.tinlok.net.IPProtocol
import tf.veriny.tinlok.net.SocketAddress

/**
 * A socket address for TCP sockets.
 */
public class TcpSocketAddress(
    private val connections: Set<TcpConnectionInfo>,
    override val hostname: String?,
) : SocketAddress<TcpConnectionInfo>(), Set<TcpConnectionInfo> by connections {
    public companion object {
        /**
         * Creates a new [TcpSocketAddress] from a singular [TcpConnectionInfo].
         */
        public fun of(info: TcpConnectionInfo, host: String? = null): TcpSocketAddress {
            return TcpSocketAddress(setOf(info), host ?: info.ip.toString())
        }
    }

    override val protocol: IPProtocol get() = IPProtocol.IPPROTO_TCP

    override fun hashCode(): Int {
        return connections.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is TcpSocketAddress) return false
        if (!connections.containsAll(other.connections)) return false

        return true
    }

    override fun toString(): String {
        return "TcpSocketAddress${connections}"
    }

}
