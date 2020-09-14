/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.exc.ClosedException
import tf.lotte.knste.net.socket.StandardSocketOption
import tf.lotte.knste.system.FD
import tf.lotte.knste.system.Syscall
import tf.lotte.knste.util.Unsafe

/**
 * Parent class for the two TCP socket classes.
 */
internal abstract class LinuxTcpParent : TcpSocket {
    /* socket has been opened and fd != -1 */
    protected var isOpen: Boolean = false
    /* linux socket file descriptor */
    protected abstract val fd: FD

    @OptIn(Unsafe::class)
    override fun <T> getSocketOption(option: StandardSocketOption<T>): T {
        if (!isOpen) throw ClosedException("This socket is not open")
        return Syscall.getsockopt(fd, option)
    }

    @OptIn(Unsafe::class)
    override fun <T> setSocketOption(option: StandardSocketOption<T>, value: T) {
        if (!isOpen) throw ClosedException("This socket is not open")
        Syscall.setsockopt(fd, option, value)
    }

    @OptIn(Unsafe::class)
    override fun close() {
        if (isOpen) return
        Syscall.close(fd)
        isOpen = false
    }
}
