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
import tf.lotte.cc.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall

/**
 * Implements a TCP socket on Linux.
 */
internal class LinuxTcpSocket(
    fd: FD, override val remoteAddress: TcpConnectionInfo
) : LinuxTcpParent(fd), TcpClientSocket {

    @OptIn(Unsafe::class)
    override fun sendEof() {
        Syscall.shutdown(fd, SHUT_WR)
    }

    @OptIn(Unsafe::class)
    override fun readInto(buf: ByteArray, size: Int, offset: Int): Int {
        val result = wrapper.read(buf, size, offset)
        if (!result.isSuccess) {
            error("The underlying socket is in non-blocking mode, but we are a blocking socket")
        }

        return result.count.toInt()
    }

    @OptIn(Unsafe::class)
    override fun writeAllFrom(buf: ByteArray): Int {
        val result = wrapper.write(buf, size = buf.size, offset = 0)
        if (!result.isSuccess) {
            error("The underlying socket is in non-blocking mode, but we are a blocking socket")
        }

        return result.count.toInt()
    }
}
