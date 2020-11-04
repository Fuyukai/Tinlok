/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import platform.posix.O_NONBLOCK
import tf.lotte.cc.Closeable
import tf.lotte.cc.Unsafe
import tf.lotte.cc.exc.ClosedException
import tf.lotte.cc.exc.OSException
import tf.lotte.cc.io.async.Selectable
import tf.lotte.cc.io.async.SelectionKey
import tf.lotte.cc.util.flagged
import tf.lotte.tinlok.net.socket.LinuxSocketOption
import tf.lotte.tinlok.net.socket.StandardSocketOption
import tf.lotte.tinlok.system.*
import tf.lotte.tinlok.util.AtomicBoolean

/**
 * A wrapper class around a file descriptor. Wraps various platform calls into an OO API.
 */
public open class FdWrapper(
    /** The actual file descriptor */
    public val fd: FD,
) : Selectable, Closeable {

    /** If this FD wrapper is open. */
    public val isOpen: AtomicBoolean = AtomicBoolean(true)

    /** If this file descriptor is non-blocking. */
    public var nonBlocking: Boolean
        get() {
            val flags = fcntl(FcntlParam.F_GETFL)
            return flagged(flags, O_NONBLOCK)
        }

        set(value: Boolean) {
            var flags = fcntl(FcntlParam.F_GETFL)
            flags = if (value) flags.or(O_NONBLOCK) else flags.and(O_NONBLOCK.inv())
            fcntl(FcntlParam.F_SETFL, flags)
        }

    /** Performs a File CoNTroL call with no parameters. */
    @OptIn(Unsafe::class)
    @Throws(ClosedException::class, OSException::class)
    public fun fcntl(param: FcntlParam<*>): Int {
        if (!isOpen) throw ClosedException("underlying fd is closed")
        return Syscall.fcntl(fd, param)
    }

    /** Performs a File CoNTroL call with one int parameter. */
    @OptIn(Unsafe::class)
    @Throws(ClosedException::class, OSException::class)
    public fun fcntl(param: FcntlParam<Int>, arg: Int): Int {
        if (!isOpen) throw ClosedException("underlying fd is closed")
        return Syscall.fcntl(fd, param, arg)
    }

    /**
     * Copies incoming data from the underlying file descriptor into the specified buffer.
     */
    @OptIn(Unsafe::class)
    @Throws(ClosedException::class, OSException::class)
    public fun read(buf: ByteArray, size: Int = buf.size, offset: Int = 0): BlockingResult {
        if (!isOpen) throw ClosedException("underlying fd is closed")

        return Syscall.read(fd, buf, count = size, offset = offset)
    }

    /**
     * Copies outgoing data from the specified buffer into the outgoing file descriptor.
     */
    @OptIn(Unsafe::class)
    @Throws(ClosedException::class, OSException::class)
    public fun write(buf: ByteArray, size: Int = buf.size, offset: Int = 0): BlockingResult {
        if (!isOpen) throw ClosedException("underlying fd is closed")

        return Syscall.write(fd, buf, size, offset)
    }

    /**
     * Gets a socket option. This will throw [UnsupportedOperationException] on non-sockets.
     */
    @OptIn(Unsafe::class)
    @Throws(UnsupportedOperationException::class)
    public fun <T> getSocketOption(option: LinuxSocketOption<T>): T {
        if (!isOpen) throw ClosedException("underlying fd is closed")

        return Syscall.getsockopt(fd, option)
    }

    @OptIn(Unsafe::class)
    @Throws(ClosedException::class, OSException::class)
    public fun <T> setSocketOption(option: StandardSocketOption<T>, value: T) {
        if (!isOpen) throw ClosedException("underlying fd is closed")

        Syscall.setsockopt(fd, option, value)
    }

    /**
     * Gets the selection key for this wrapper.
     */
    override fun key(): SelectionKey {
        if (!isOpen) throw ClosedException("underlying fd is closed")

        return FdSelectionKey(fd)
    }

    /**
     * Closes the file descriptor this object is wrapping.
     */
    @OptIn(Unsafe::class)
    override fun close() {
        if (!isOpen.compareAndSet(expected = true, new = false)) return

        Syscall.close(fd)
    }
}
