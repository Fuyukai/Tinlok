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
import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.exc.OSException
import tf.lotte.tinlok.net.AddressFamily
import tf.lotte.tinlok.net.AllConnectionsFailedException
import tf.lotte.tinlok.net.tcp.*
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall

@Unsafe
public actual object PlatformSockets {
    /**
     * Attempts a TCP connection, returning a pair of (fd, TcpConnectionInfo) for the successful
     * connection attempt.
     */
    @Unsafe
    @Throws(AllConnectionsFailedException::class, OSException::class)
    public fun tryTcpConnect(
        address: TcpSocketAddress, timeout: Int
    ): Pair<FD, TcpConnectionInfo> {
        // try every address in sequence
        // when kotlin's concurrency (memory) model gets better, i will implement happy eyeballs.
        // this swallows ENETUNREACH, and various other errors, but that's a valid tradeoff for now.

        // naiive algorithm
        for (info in address) {
            val socket = Syscall.socket(info.family, info.type, info.protocol)
            try {
                Syscall.__connect_blocking(socket, info, timeout)

                // no error was hit during connect, we are now connected
                return Pair(socket, info)
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
     * Creates a new connected [TcpClientSocket].
     */
    @Unsafe
    @Throws(AllConnectionsFailedException::class, OSException::class)
    public actual fun newTcpSynchronousSocket(
        address: TcpSocketAddress, timeout: Int
    ): TcpClientSocket {
        val (fd, info) = tryTcpConnect(address, timeout)
        return LinuxTcpSocket(fd, info)
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
