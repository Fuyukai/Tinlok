/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import platform.posix.SHUT_WR
import tf.lotte.cc.Unsafe
import tf.lotte.tinlok.exc.ClosedException
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall

/**
 * Implements a TCP socket on Linux.
 */
internal class LinuxTcpSocket(
    override val fd: FD, address: TcpConnectionInfo
) : LinuxTcpParent(), TcpClientSocket {
    init {
        // this socket is always open initially
        isOpen.value = true
    }

    override val remoteAddress = address

    @OptIn(Unsafe::class)
    override fun sendEof() {
        Syscall.shutdown(fd, SHUT_WR)
    }

    @OptIn(Unsafe::class)
    override fun readInto(buf: ByteArray, offset: Int, bytes: Int): Int {
        if (!isOpen.value) throw ClosedException("This socket is closed")
        return Syscall.recv(fd, buffer = buf, offset = offset)
    }

    @OptIn(Unsafe::class)
    override fun writeAllFrom(buf: ByteArray): Int {
        if (!isOpen.value) throw ClosedException("This socket is closed")
        return Syscall.send(fd, buf).toInt()
    }
}
