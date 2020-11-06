/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.cc.exc.TimeoutException
import tf.lotte.cc.io.readUpTo
import tf.lotte.cc.net.IPv4Address
import tf.lotte.cc.net.tcp.TcpConnectionInfo
import tf.lotte.cc.net.tcp.TcpSocketAddress
import tf.lotte.tinlok._workarounds.__Workaround_TcpSocketTests
import tf.lotte.tinlok.net.connect
import tf.lotte.tinlok.net.resolve
import kotlin.test.*

// Welcome to the rest of your life.
// Kotlin/Native concurrency is *completely* broken.
// Kotlin MPP (as far as I can tell) does not let us create a nativeTest source set.
// So, instead,
// In order to not fucking copy the tests every time,
// part of these tests are specified in nativeMain (and as a result copied to the final library).
// This lets us actually access the Worker class, and open a new TCP server socket.

/**
 * Tests the TcpSocket class.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class `Test TcpSocket` {
    @Test
    fun `Test basic socket read`() {
        // d-g selected because it is dual stack
        val address = TcpSocketAddress.resolve("time-d-b.nist.gov", 13)
        val data = TcpClientSocket.connect(address) {
            it.readUpTo(4096)
        }

        // NIST time output is always in a nice format
        assertNotNull(data)
        val decoded = data.decode()
        // hack job to make sure we received DAYTIME protocol data
        assertTrue(decoded.startsWith('\n'))
        assertTrue(decoded.endsWith('\n'))

        val split = decoded.split(' ')
        assertTrue(split.contains("UTC(NIST)"))
    }

    @Test
    fun `Test socket accept`() {
        __Workaround_TcpSocketTests.testSocketAccept()
    }

    /**
     * Ensures timeing out works properly.
     */
    @Test
    fun `Test socket connect timeout`() {
        // random port, should ideally never connect
        val ip = IPv4Address.of("127.0.0.1")
        val address = TcpSocketAddress.of(TcpConnectionInfo(ip, 22334))

        assertFailsWith<TimeoutException> {
            TcpClientSocket.connect(address, timeout = 1_000) {}
        }

    }
}
