/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.io.Buffer
import tf.lotte.tinlok.io.HalfCloseableStream
import tf.lotte.tinlok.net.ConnectionInfo
import tf.lotte.tinlok.net.StandardSocketTypes
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.system.ensureNonBlock
import tf.lotte.tinlok.util.Closeable
import tf.lotte.tinlok.util.ClosingScope
import tf.lotte.tinlok.util.use
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * An [HalfCloseableStream] that wraps a [Socket].
 */
public class SynchronousSocketStream<I : ConnectionInfo>
public constructor(public val socket: Socket<I>) : HalfCloseableStream, Closeable by socket {
    public companion object {
        /**
         * Connects a [SynchronousSocketStream] to the specified [address], with the specified
         * [timeout].
         */
        @OptIn(Unsafe::class, ExperimentalContracts::class)
        public inline fun <R> tcpConnect(
            address: TcpSocketAddress,
            timeout: Int = 30_000,
            block: (SynchronousSocketStream<TcpConnectionInfo>) -> R
        ): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            val socket = happyEyeballsTcpConnect(address, timeout)
            val stream = SynchronousSocketStream(socket)
            return stream.use(block)
        }

        /**
         * Connects a [SynchronousSocketStream] to the specified [address], with the specified
         * [timeout], adding it to the specified [scope].
         */
        @OptIn(Unsafe::class)
        public inline fun <R> tcpConnect(
            scope: ClosingScope,
            address: TcpSocketAddress,
            timeout: Int = 30_000
        ): SynchronousSocketStream<TcpConnectionInfo> {
            val socket = happyEyeballsTcpConnect(address, timeout)
            val stream = SynchronousSocketStream(socket)
            scope.add(stream)
            return stream
        }
    }

    init {
        require(socket.type == StandardSocketTypes.SOCK_STREAM) {
            "This class only works on streaming sockets"
        }

        require(!socket.nonBlocking) {
            "This class only works on non-blocking sockets"
        }
    }

    /**
     * Reads data into [ba], with the specified [size] and [offset].
     */
    override fun readInto(ba: ByteArray, size: Int, offset: Int): Int {
        val result = socket.recv(ba, size = size, offset = offset, flags = 0).ensureNonBlock()
        return result.toInt()
    }

    /**
     * Reads data into [buffer], with the specified [size].
     */
    override fun readInto(buffer: Buffer, size: Int): Int {
        val result = socket.recv(buffer, size, flags = 0).ensureNonBlock()
        return result.toInt()
    }

    /**
     * Attempts to write the entirety of the ByteArray [ba] to this object, returning the number of
     * bytes actually written before reaching EOF.
     */
    @OptIn(Unsafe::class)
    override fun writeAllFrom(ba: ByteArray): Int {
        // retry loop to ensure all is written
        var written = 0
        while (true) {
            val result = socket.send( ba, ba.size - written, written, 0).ensureNonBlock()
            written += result.toInt()

            if (written >= ba.size) {
                break
            }
        }

        return written
    }

    /**
     * Attempts to write the entirety of the buffer [buffer] from the cursor onwards to this object,
     * returning the number of bytes actually written before reaching EOF.
     */
    @OptIn(Unsafe::class)
    override fun writeAllFrom(buffer: Buffer): Int {
        // ensure we're not trying to write to a buffer with no space left
        if (buffer.cursor >= buffer.capacity - 1) return 0
        return socket.retrySend(buffer).ensureNonBlock().toInt()
    }

    /**
     * Shuts down the write end of this socket.
     */
    override fun sendEof() {
        socket.shutdown(ShutdownOption.WRITE)
    }
}

