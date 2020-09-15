/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.net.ConnectionInfoCreator
import tf.lotte.knste.system.FD
import tf.lotte.knste.system.Syscall
import tf.lotte.knste.util.Unsafe

/**
 * Implements Linux TCP server socket that produces new [LinuxTcpSocket] children.
 */
internal class LinuxTcpServerSocket : LinuxTcpParent(), TcpServerSocket {
    override var fd: FD = -1

    @OptIn(Unsafe::class)
    override fun bind(address: TcpConnectionInfo, backlog: Int) {
        val sock = Syscall.socket(address.family, address.type, address.protocol)
        try {
            Syscall.bind(sock, address)
            Syscall.listen(sock, backlog)
            fd = sock
        } catch (e: Throwable) {
            // ensure the socket is always closed if bind() or listen() fail
            Syscall.close(sock)
            throw e
        }
    }

    @Unsafe
    override fun unsafeAccept(): TcpClientSocket {
        val accepted = Syscall.accept(fd, ConnectionInfoCreator.Tcp)
        return LinuxTcpSocket(accepted.fd, accepted.info!!)
    }
}
