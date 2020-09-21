/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import platform.posix.ENETUNREACH
import tf.lotte.tinlok.exc.OSException
import tf.lotte.tinlok.net.AddressFamily
import tf.lotte.tinlok.net.AllConnectionsFailedException
import tf.lotte.tinlok.net.tcp.*
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.Unsafe

@Unsafe
public actual object PlatformSockets {
    /**
     * Creates a new unconnected [TcpClientSocket].
     */
    @Unsafe
    @Throws(AllConnectionsFailedException::class, OSException::class)
    public actual fun newTcpSynchronousSocket(address: TcpSocketAddress): TcpClientSocket {
        // try every address in sequence
        // when kotlin's concurrency (memory) model gets better, i will implement happy eyeballs.
        // this swallows ENETUNREACH, and various other errors, but that's a valid tradeoff for now.

        // naiive algorithm
        // TODO: Maybe re-throw a nicer error on connect() errno?
        for (info in address) {
            val socket = Syscall.socket(info.family, info.type, info.protocol)
            try {
                Syscall.connect(socket, info)

                // no error was hit during connect, we are now connected
                return LinuxTcpSocket(socket, info)
            } catch (e: OSException) {
                // unconditionally close, we can't really do anything
                Syscall.close(socket)

                // ENETUNREACH is raised when ipv6 is requested but the network doesn't support ipv6
                // so silently eat the error
                if (e.errno == ENETUNREACH && info.family == AddressFamily.AF_INET6) continue
                else throw e
            } catch (e: Throwable) {
                // always close if connect() fails for other reasons
                Syscall.close(socket)
                throw e
            }
        }

        throw AllConnectionsFailedException(address)
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
