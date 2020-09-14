/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.net.connect
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Welcome to the rest of your life.

/**
 * Tests the TcpSocket class.
 */
@OptIn(ExperimentalUnsignedTypes::class)
class `Test TcpSocket` {
    @Test
    fun `Test basic socket read`() {
        val address = TcpSocketAddress.resolve("time-a.nist.gov", 13)
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
}
