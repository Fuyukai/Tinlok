/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.tinlok.net.ConnectionInfoCreator
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.Unsafe

/**
 * Implements Linux TCP server socket that produces new [LinuxTcpSocket] children.
 */
internal class LinuxTcpServerSocket(
    override val fd: FD,
    private val address: TcpConnectionInfo
) : LinuxTcpParent(), TcpServerSocket {
    @OptIn(Unsafe::class)
    override fun bind(backlog: Int) {
        Syscall.bind(fd, address)
        Syscall.listen(fd, backlog)
        isOpen = true
    }

    @Unsafe
    override fun unsafeAccept(): TcpClientSocket {
        val accepted = Syscall.accept(fd, ConnectionInfoCreator.Tcp)
        return LinuxTcpSocket(accepted.fd, accepted.info!!)
    }
}
