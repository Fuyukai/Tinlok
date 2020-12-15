/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.io.readUpTo
import tf.lotte.tinlok.io.writeAll
import tf.lotte.tinlok.net.resolve
import tf.lotte.tinlok.net.tcp.TcpSocketAddress
import tf.lotte.tinlok.util.ClosingScope
import tf.lotte.tinlok.util.b
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val CERT = """-----BEGIN CERTIFICATE-----
MIIC0TCCAlagAwIBAgIUMh0kWgXIMA3Dw1n6Y6huCl9rzl0wCgYIKoZIzj0EAwIw
gZ4xCzAJBgNVBAYTAkFCMRMwEQYDVQQIDApTb21lIFN0YXRlMRIwEAYDVQQHDAlT
b21lIENpdHkxFTATBgNVBAoMDFNvbWUgQ29tcGFueTEVMBMGA1UECwwMU29tZSBT
ZWN0aW9uMRkwFwYDVQQDDBBzb21lLmRvbWFpbi5uYW1lMR0wGwYJKoZIhvcNAQkB
Fg5zb21lQGVtYWlsLmNvbTAeFw0yMDExMjcwNzQ0NTZaFw0zMDExMjUwNzQ0NTZa
MIGeMQswCQYDVQQGEwJBQjETMBEGA1UECAwKU29tZSBTdGF0ZTESMBAGA1UEBwwJ
U29tZSBDaXR5MRUwEwYDVQQKDAxTb21lIENvbXBhbnkxFTATBgNVBAsMDFNvbWUg
U2VjdGlvbjEZMBcGA1UEAwwQc29tZS5kb21haW4ubmFtZTEdMBsGCSqGSIb3DQEJ
ARYOc29tZUBlbWFpbC5jb20wdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAS12moBvZ4z
wprhOvDytT3xhGzm2AT/kyu4qRuJ00pIUrikKbdNODDj5SIvBKTMU3H+yffgy04N
1Q8ylxu8qM+AmCu4GGEVwnMTEgqUPwnBr+ABb0ar3UEVl2ZXY2ATitejUzBRMB0G
A1UdDgQWBBR7N2BQZ9AIWslGvAYWWSDcWJGL8DAfBgNVHSMEGDAWgBR7N2BQZ9AI
WslGvAYWWSDcWJGL8DAPBgNVHRMBAf8EBTADAQH/MAoGCCqGSM49BAMCA2kAMGYC
MQCqlXQO/K5qONH5QhBtRZLNz2z4UbgJV91pkR++vOW2Yf+A/22lqqyI8IOnTDCi
0/oCMQCtQFJsXsRisxVaG2Kv7MXE8new9V0jne960t18aG2p9ylgUgzmIkh+9dcW
cDswUmM=
-----END CERTIFICATE-----"""

private const val KEY = """-----BEGIN PRIVATE KEY-----
MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDDKaipS6nCXsNU3O/l1
fMmTHDOJsbMVH3NyzLNWSB/atGxYwrC5Vs8PisW7Ow94S3ShZANiAAS12moBvZ4z
wprhOvDytT3xhGzm2AT/kyu4qRuJ00pIUrikKbdNODDj5SIvBKTMU3H+yffgy04N
1Q8ylxu8qM+AmCu4GGEVwnMTEgqUPwnBr+ABb0ar3UEVl2ZXY2ATitc=
-----END PRIVATE KEY-----"""

/**
 * Tests TLS functionality.
 */
@OptIn(Unsafe::class)
public class `Test TLS` {
    private companion object {
        //private val SERVER_CONFIG = TlsServerConfig(certificatePem = CERT, privateKeyPem = KEY)
        private val CLIENT_CONFIG = TlsClientConfig().apply {
            addTrustedCertificate(CERT)
        }

        // don't care if these get leaked
        private val CLIENT_CONTEXT = TlsContext(CLIENT_CONFIG)
    }

    /**
     * Tests a valid connection to a TLS server.
     */
    @Test
    public fun `Test valid connection`() = ClosingScope {
        val addr = TcpSocketAddress.resolve("sha512.badssl.com", 443)
        val tlsStream = SynchronousTlsStream.unsafeConnect(CLIENT_CONTEXT, addr)
        it.add(tlsStream)
        tlsStream.writeAll(b("GET / HTTP/1.1\r\nhost:sha512.badssl.com\r\n\r\n"))
        val result = tlsStream.readUpTo(2048)
        assertNotNull(result)
        assertTrue(result.startsWith(b("HTTP/1.1 200 OK")))
    }
}
