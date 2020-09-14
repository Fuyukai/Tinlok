/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.ByteString
import tf.lotte.knste.exc.ClosedException
import tf.lotte.knste.system.FD
import tf.lotte.knste.system.Syscall
import tf.lotte.knste.util.Unsafe

/**
 * Implements a TCP socket on Linux.
 */
internal class LinuxTcpSocket(
    override val fd: FD, address: TcpConnectionInfo
) : LinuxTcpParent(), TcpClientSocket {
    init {
        // this socket is always open
        isOpen = true
    }

    override val remoteAddress = address

    override fun sendEof() {
        TODO("not implemented")
    }

    @OptIn(Unsafe::class)
    override fun readUpTo(bytes: Long): ByteString? {
        if (!isOpen) throw ClosedException("This socket is not opened yet")

        val buffer = ByteArray(bytes.toInt())
        val size = Syscall.recv(fd, buffer)
        // EOF
        if (size == 0) return null

        return if (size == buffer.size) {
            ByteString.fromUncopied(buffer)
        } else {
            // ugh
            val copy = buffer.copyOfRange(0, size)
            ByteString.fromUncopied(copy)
        }
    }

    @OptIn(Unsafe::class)
    override fun writeAll(bs: ByteString) {
        if (!isOpen) throw ClosedException("This socket is not opened yet")
        val unwrapped = bs.unwrap()
        Syscall.send(fd, unwrapped)
    }
}
