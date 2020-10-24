/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.exc.ClosedException
import tf.lotte.tinlok.net.ConnectionInfoCreator
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall

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
        isOpen.value = true
    }

    @Unsafe
    override fun unsafeAccept(): TcpClientSocket {
        if (!isOpen.value) throw ClosedException("This socket is closed")
        val accepted = Syscall.accept(fd, ConnectionInfoCreator.Tcp)
        return LinuxTcpSocket(accepted.fd, accepted.info!!)
    }
}
