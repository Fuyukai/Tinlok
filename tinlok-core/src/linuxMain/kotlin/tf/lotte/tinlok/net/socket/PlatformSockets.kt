/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.exc.SocketException
import tf.lotte.tinlok.net.tcp.*
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.Unsafe

@Unsafe
public actual object PlatformSockets {
    /**
     * Creates a new unconnected [TcpClientSocket].
     */
    @Unsafe
    public actual fun newTcpSynchronousSocket(address: TcpSocketAddress): TcpClientSocket {
        // try every address in sequence
        // when kotlin's concurrency (memory) model gets better, i will implement happy eyeballs.

        // naiive algorithm
        // TODO: Maybe re-throw a nicer error on connect() errno?
        for (info in address) {
            val socket = Syscall.socket(info.family, info.type, info.protocol)
            val connected = try {
                Syscall.connect(socket, info)
            } catch (e: Throwable) {
                // always close if connect() fails
                Syscall.close(socket)
                throw e
            }

            // success
            if (connected) {
                return LinuxTcpSocket(socket, info)
            }
        }

        throw SocketException(message = "All attempts at connecting to $address failed")
    }

    /**
     * Creates a new unbound [TcpServerSocket].
     */
    @Unsafe
    public actual fun newTcpSynchronousServerSocket(address: TcpConnectionInfo): TcpServerSocket {
        val fd = Syscall.socket(address.family, address.type, address.protocol)
        val sock = LinuxTcpServerSocket(fd, address)
        return sock
    }
}
