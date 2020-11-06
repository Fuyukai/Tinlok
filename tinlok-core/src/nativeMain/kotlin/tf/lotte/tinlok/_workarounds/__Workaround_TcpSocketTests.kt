/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok._workarounds

import tf.lotte.cc.io.readUpTo
import tf.lotte.cc.io.writeAll
import tf.lotte.cc.net.tcp.TcpConnectionInfo
import tf.lotte.cc.net.tcp.TcpSocketAddress
import tf.lotte.cc.types.b
import tf.lotte.tinlok.net.*
import tf.lotte.tinlok.net.tcp.TcpClientSocket
import tf.lotte.tinlok.net.tcp.TcpServerSocket
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Actual cross-platform definition of TCP socket tests.
 */
internal actual object __Workaround_TcpSocketTests {
    actual fun testSocketAccept() {
        val worker = Worker.start(name = "IHateThisLanguage")

        worker.execute(TransferMode.SAFE, {}) {
            val addr = TcpConnectionInfo.wildcard(4949)
            TcpServerSocket.bind(addr, 1) {
                // only accept once
                it.accept { client ->
                    // echo one message back
                    val data = client.readUpTo(4096)
                    if (data != null) client.writeAll(data)
                }
            }
        }

        // allow the socket to bind itself
        provisional_sleep(1000L)

        val sock = TcpSocketAddress.resolve("::1", 4949)
        TcpClientSocket.connect(sock) {
            val toWrite = b("hello, world!")
            it.writeAll(toWrite)

            // off-thread server will echo it back
            val data = it.readUpTo(4096)
            assertNotNull(data)
            assertEquals(data, toWrite)
        }
    }
}
