/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.socket

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.io.NetworkUnreachableException
import tf.veriny.tinlok.net.AddressFamily
import tf.veriny.tinlok.net.AllConnectionsFailedException
import tf.veriny.tinlok.net.tcp.TcpConnectionInfo
import tf.veriny.tinlok.net.tcp.TcpSocketAddress

// Blatant lie! K/N concurrency is too primitive for this to be really Happy Eyeballs.
/**
 * Attempts a synchronous happy eyeballs connect.
 */
@Unsafe
public fun happyEyeballsTcpConnect(
    address: TcpSocketAddress,
    timeout: Int = 30_000,
): Socket<TcpConnectionInfo> {
    for (info in address) {
        val sock = Socket.tcp(info.family)
        try {
            sock.connect(info, timeout = timeout)
            return sock
        } catch (e: NetworkUnreachableException) {
            // ENETUNREACH is raised when ipv6 is requested but the network doesn't support ipv6
            // so silently eat the error
            if (info.family == AddressFamily.AF_INET6) continue
            else {
                // an actual unreachable network erro
                sock.close()
                throw e
            }
        } catch (e: Throwable) {
            // always close the underlying socket, then re-throw the error
            sock.close()
            throw e
        }
    }

    throw AllConnectionsFailedException(address)
}
