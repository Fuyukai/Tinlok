/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.cc.net.ConnectionInfo
import tf.lotte.cc.net.IPAddress
import tf.lotte.cc.net.tcp.TcpConnectionInfo

/**
 * Defines a connection information creator, to turn an IP address into a ConnectionInfo.
 */
public interface ConnectionInfoCreator<T : ConnectionInfo> {
    /**
     * An information creator for [TcpConnectionInfo] instances.
     */
    public object Tcp : ConnectionInfoCreator<TcpConnectionInfo> {
        override fun from(ip: IPAddress, port: Int): TcpConnectionInfo {
            return TcpConnectionInfo(ip, port)
        }
    }

    /**
     * Creates a new [T] from the IP address and port specified.
     */
    public fun from(ip: IPAddress, port: Int = 0): T
}
