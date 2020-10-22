/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.types.bytestring.b
import kotlin.test.*

// BIG thanks to BadSSL for existing
public class `Test Tls Client` {
    /**
     * Verifies the specified server returns a correct HTTP response, and works over TLS.
     */
    private fun verify(host: String, port: Int = 443, config: TlsConfig = TlsConfig.DEFAULT) {
        val addr = TcpSocketAddress.resolve(host, port)
        TlsClientSocket.connect(addr, config) { sock ->
            val toWrite = b("GET / HTTP/1.1\r\nHost: $host:$port\r\n\r\n")
            sock.writeAllFrom(toWrite)

            // won't fit, but we only check for a starts with
            val data = sock.readUpTo(256)
            assertNotNull(data)
            val decoded = data.decode()
            assertTrue(decoded.startsWith("HTTP/1.1 200 OK\r\n"))
        }
    }

    @Test
    public fun `Test valid TLS connection`() {
        verify("sha256.badssl.com")
        verify("rsa2048.badssl.com")
        verify("ecc256.badssl.com")
        verify("ecc384.badssl.com")
        verify("extended-validation.badssl.com")
        verify("mozilla-modern.badssl.com")
    }

    @Test
    public fun `Test untrusted TLS connection`() {
        assertFailsWith<TlsException> { verify("expired.badssl.com") }
        assertFailsWith<TlsException> { verify("wrong.host.badssl.com") }
        assertFailsWith<TlsException> { verify("self-signed.badssl.com") }
        assertFailsWith<TlsException> { verify("untrusted-root.badssl.com") }
    }

    @Test
    public fun `Test legacy cryptography`() {
        assertFailsWith<TlsException> { verify("tls-v1-0.badssl.com", 1010) }
        assertFailsWith<TlsException> { verify("tls-v1-1.badssl.com", 1011) }
    }

    @Test
    public fun `Test bad/weak cryptography`() {
        assertFailsWith<TlsException> { verify("rc4-md5.badssl.com") }
        assertFailsWith<TlsException> { verify("null.badssl.com") }
    }
}
