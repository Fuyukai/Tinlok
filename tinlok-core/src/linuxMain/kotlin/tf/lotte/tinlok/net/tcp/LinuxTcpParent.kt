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
import tf.lotte.tinlok.io.FdWrapper
import tf.lotte.tinlok.net.socket.StandardSocketOption
import tf.lotte.tinlok.system.FD

/**
 * Parent class for the two TCP socket classes.
 */
public abstract class LinuxTcpParent(
    protected val fd: FD,
) : TcpSocket {
    /* wrapper object around the file descriptor */
    protected val wrapper: FdWrapper = FdWrapper(fd)

    @OptIn(Unsafe::class)
    override fun <T> getSocketOption(option: StandardSocketOption<T>): T {
        return wrapper.getSocketOption(option)
    }

    @OptIn(Unsafe::class)
    override fun <T> setSocketOption(option: StandardSocketOption<T>, value: T) {
        return wrapper.setSocketOption(option, value)
    }

    @OptIn(Unsafe::class)
    override fun close() {
        wrapper.close()
    }
}
