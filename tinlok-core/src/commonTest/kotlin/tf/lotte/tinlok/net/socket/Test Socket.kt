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
import tf.lotte.tinlok.io.ByteArrayBuffer
import tf.lotte.tinlok.io.ofSize
import tf.lotte.tinlok.net.StandardAddressFamilies
import tf.lotte.tinlok.net.tcp.TcpConnectionInfo
import tf.lotte.tinlok.util.ClosingScope
import kotlin.test.*

/**
 * Tests sockets.
 */
public class `Test Socket` {
    /**
     * Ensures setting and retrieving a boolean socket option works.
     */
    @Test
    @OptIn(Unsafe::class)
    public fun `Test socket boolean option`() = ClosingScope {
        val socket = Socket.tcp(StandardAddressFamilies.AF_INET6)
        it.add(socket)
        // reasonable option
        assertFalse(socket.getOption(StandardSocketOptions.SO_REUSEADDR))
        socket.setOption(StandardSocketOptions.SO_REUSEADDR, true)
        assertTrue(socket.getOption(StandardSocketOptions.SO_REUSEADDR))
    }

    /**
     * Ensures setting and retrieving a ULong socket option works.
     */
    @Test
    @OptIn(Unsafe::class, ExperimentalUnsignedTypes::class)
    public fun `Test socket ULong option`() = ClosingScope {
        val socket = Socket.tcp(StandardAddressFamilies.AF_INET6)
        it.add(socket)

        // note: the kernel doubles this on linux, so we have to test both the normal and double
        // version
        // also, low values seem to be forcibly set higher? unsure
        val count = 5096UL
        socket.setOption(StandardSocketOptions.SO_RCVBUF, count)
        val result = socket.getOption(StandardSocketOptions.SO_RCVBUF)
        assertTrue(setOf(count, count * 2UL).contains(result))
    }

    /**
     * Ensures non-blocking is set properly.
     */
    @Test
    @OptIn(Unsafe::class)
    public fun `Test socket nonblocking`() = ClosingScope {
        val socket = Socket.tcp(StandardAddressFamilies.AF_INET6)
        // Not safe on Windows: But it doesn't matter. This is test code.
        socket.setOption(StandardSocketOptions.SO_REUSEADDR, true)
        it.add(socket)

        socket.nonBlocking = true
        // on linux, at least, this does a second fcntl. either way, if the set failed
        // then the fcntl/ioctlsocket should have failed so this is just an extra safety check
        assertTrue(socket.nonBlocking)

        // attempt to do a blocking operation, like accept() (without a connection)
        socket.bind(TcpConnectionInfo.wildcard(5555))
        socket.listen(1)

        // intellij doesn't see this as nullable for some reason?
        // but it is null (so no connections outstanding)
        val result: Socket<TcpConnectionInfo>? = socket.accept()
        assertNull(result)
    }

    // TODO: Use select/poll to ensure this works less flakily.
    /**
     * Tests accepting a new socket connection.
     */
    @Test
    @OptIn(Unsafe::class)
    public fun `Test socket accept`(): Unit = ClosingScope {
        val server = Socket.tcp(StandardAddressFamilies.AF_INET6)
        server.setOption(StandardSocketOptions.SO_REUSEADDR, true)
        it.add(server)

        // same fanfare as above.
        server.nonBlocking = true
        server.bind(TcpConnectionInfo.wildcard(5556))
        server.listen(1)

        // outgoing connection to the accepting socket
        val clientLocal = Socket.tcp(StandardAddressFamilies.AF_INET6)
        it.add(clientLocal)
        clientLocal.nonBlocking = true
        // non-blocking connect
        clientLocal.connect(TcpConnectionInfo.localhost(5556), timeout = 0)

        // incoming connection from the accepting socket
        // retry loop because on windows it can take a little while to actually connect over
        // loopback.
        var clientRemote: Socket<TcpConnectionInfo>? = null
        for (try_ in 0..1000) {
            clientRemote = server.accept()
            @Suppress("SENSELESS_COMPARISON")  // IntelliJ lies!
            if (clientRemote != null) break
        }

        assertTrue(clientRemote != null)
        it.add(clientRemote)
        assertTrue(clientRemote.nonBlocking)

        // a read on the remote should fail, as we have no data
        val read1 = clientRemote.recv(byteArrayOf(0, 0, 0, 0), 4, 0, 0)
        assertFalse(read1.isSuccess)

        // now we write some data into the socket, to test i/o
        val outgoing = ByteArrayBuffer.ofSize(8)
        outgoing.writeInt(52194054)
        // rewind
        outgoing.cursor = 0L

        // all kernels will buffer a little bit, on both ends, so this should always be good
        val written = clientLocal.sendall(outgoing, 4, 0)
        assertTrue(written.isSuccess)
        assertTrue(written.count == 4L)

        // now we read the data we just sent!
        val incoming = ByteArrayBuffer.ofSize(8)
        // request a LARGER read than what we actually have
        val read = clientRemote.recv(incoming, 8, 0)
        assertTrue(read.isSuccess)
        // we obviously only read 4 bytes, since that's all that was sent
        assertTrue(read.count == 4L)

        incoming.cursor = 0
        val int = incoming.readInt()
        assertEquals(52194054, int)
    }
}
