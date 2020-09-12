/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.dns

import tf.lotte.knste.net.AddressFamily
import tf.lotte.knste.net.tcp.TcpConnectionInfo
import tf.lotte.knste.util.Unsafe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests the global DNS resolver of a platform.
 */
@OptIn(Unsafe::class)
class `Test GlobalResolver` {
    @Test
    fun `Test resolving`() {
        // only address I can think of that remains static
        val addresses = GlobalResolver.getaddrinfo("one.one.one.one", 443)
        val tcpAddrs = addresses
            .filterIsInstance<TcpConnectionInfo>()

        assertTrue(tcpAddrs.isNotEmpty())

        run {
            val inet4 = tcpAddrs.find { it.family == AddressFamily.AF_INET }!!
            assertEquals(inet4.port, 443)
            // the ip returned may be either, so we chekc if its either
            val addrStr = inet4.ip.toString()
            assertTrue(addrStr == "1.0.0.1" || addrStr == "1.1.1.1")
        }

        run {
            val inet6 = tcpAddrs.find { it.family == AddressFamily.AF_INET6 }!!
            assertEquals(inet6.port, 443)
            val addrStr = inet6.ip.toString()
            println(addrStr)
            assertTrue(addrStr == "2606:4700:4700::1001" || addrStr == "2606:4700:4700::1111")
        }
    }
}
