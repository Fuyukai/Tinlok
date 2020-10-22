/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.tinlok.net.socket.StandardSocketOption
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.AtomicBoolean
import tf.lotte.tinlok.util.Unsafe

/**
 * Parent class for the two TCP socket classes.
 */
internal abstract class LinuxTcpParent : TcpSocket {
    /* socket has been opened and fd != -1 */
    protected val isOpen = AtomicBoolean(false)

    /* linux socket file descriptor */
    protected abstract val fd: FD

    @OptIn(Unsafe::class)
    override fun <T> getSocketOption(option: StandardSocketOption<T>): T {
        return Syscall.getsockopt(fd, option)
    }

    @OptIn(Unsafe::class)
    override fun <T> setSocketOption(option: StandardSocketOption<T>, value: T) {
        Syscall.setsockopt(fd, option, value)
    }

    @OptIn(Unsafe::class)
    override fun close() {
        if (isOpen.value) return

        Syscall.close(fd)
        isOpen.value = false
    }
}
