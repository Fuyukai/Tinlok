/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
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
import kotlin.test.*

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

private const val BADSSL_UNTRUSTED = """-----BEGIN CERTIFICATE-----
MIIGfjCCBGagAwIBAgIJAJeg/PrX5Sj9MA0GCSqGSIb3DQEBCwUAMIGBMQswCQYD
VQQGEwJVUzETMBEGA1UECAwKQ2FsaWZvcm5pYTEWMBQGA1UEBwwNU2FuIEZyYW5j
aXNjbzEPMA0GA1UECgwGQmFkU1NMMTQwMgYDVQQDDCtCYWRTU0wgVW50cnVzdGVk
IFJvb3QgQ2VydGlmaWNhdGUgQXV0aG9yaXR5MB4XDTE2MDcwNzA2MzEzNVoXDTM2
MDcwMjA2MzEzNVowgYExCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlh
MRYwFAYDVQQHDA1TYW4gRnJhbmNpc2NvMQ8wDQYDVQQKDAZCYWRTU0wxNDAyBgNV
BAMMK0JhZFNTTCBVbnRydXN0ZWQgUm9vdCBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkw
ggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQDKQtPMhEH073gis/HISWAi
bOEpCtOsatA3JmeVbaWal8O/5ZO5GAn9dFVsGn0CXAHR6eUKYDAFJLa/3AhjBvWa
tnQLoXaYlCvBjodjLEaFi8ckcJHrAYG9qZqioRQ16Yr8wUTkbgZf+er/Z55zi1yn
CnhWth7kekvrwVDGP1rApeLqbhYCSLeZf5W/zsjLlvJni9OrU7U3a9msvz8mcCOX
fJX9e3VbkD/uonIbK2SvmAGMaOj/1k0dASkZtMws0Bk7m1pTQL+qXDM/h3BQZJa5
DwTcATaa/Qnk6YHbj/MaS5nzCSmR0Xmvs/3CulQYiZJ3kypns1KdqlGuwkfiCCgD
yWJy7NE9qdj6xxLdqzne2DCyuPrjFPS0mmYimpykgbPnirEPBF1LW3GJc9yfhVXE
Cc8OY8lWzxazDNNbeSRDpAGbBeGSQXGjAbliFJxwLyGzZ+cG+G8lc+zSvWjQu4Xp
GJ+dOREhQhl+9U8oyPX34gfKo63muSgo539hGylqgQyzj+SX8OgK1FXXb2LS1gxt
VIR5Qc4MmiEG2LKwPwfU8Yi+t5TYjGh8gaFv6NnksoX4hU42gP5KvjYggDpR+NSN
CGQSWHfZASAYDpxjrOo+rk4xnO+sbuuMk7gORsrl+jgRT8F2VqoR9Z3CEdQxcCjR
5FsfTymZCk3GfIbWKkaeLQIDAQABo4H2MIHzMB0GA1UdDgQWBBRvx4NzSbWnY/91
3m1u/u37l6MsADCBtgYDVR0jBIGuMIGrgBRvx4NzSbWnY/913m1u/u37l6MsAKGB
h6SBhDCBgTELMAkGA1UEBhMCVVMxEzARBgNVBAgMCkNhbGlmb3JuaWExFjAUBgNV
BAcMDVNhbiBGcmFuY2lzY28xDzANBgNVBAoMBkJhZFNTTDE0MDIGA1UEAwwrQmFk
U1NMIFVudHJ1c3RlZCBSb290IENlcnRpZmljYXRlIEF1dGhvcml0eYIJAJeg/PrX
5Sj9MAwGA1UdEwQFMAMBAf8wCwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IC
AQBQU9U8+jTRT6H9AIFm6y50tXTg/ySxRNmeP1Ey9Zf4jUE6yr3Q8xBv9gTFLiY1
qW2qfkDSmXVdBkl/OU3+xb5QOG5hW7wVolWQyKREV5EvUZXZxoH7LVEMdkCsRJDK
wYEKnEErFls5WPXY3bOglBOQqAIiuLQ0f77a2HXULDdQTn5SueW/vrA4RJEKuWxU
iD9XPnVZ9tPtky2Du7wcL9qhgTddpS/NgAuLO4PXh2TQ0EMCll5reZ5AEr0NSLDF
c/koDv/EZqB7VYhcPzr1bhQgbv1dl9NZU0dWKIMkRE/T7vZ97I3aPZqIapC2ulrf
KrlqjXidwrGFg8xbiGYQHPx3tHPZxoM5WG2voI6G3s1/iD+B4V6lUEvivd3f6tq7
d1V/3q1sL5DNv7TvaKGsq8g5un0TAkqaewJQ5fXLigF/yYu5a24/GUD783MdAPFv
gWz8F81evOyRfpf9CAqIswMF+T6Dwv3aw5L9hSniMrblkg+ai0K22JfoBcGOzMtB
Ke/Ps2Za56dTRoY/a4r62hrcGxufXd0mTdPaJLw3sJeHYjLxVAYWQq4QKJQWDgTS
dAEWyN2WXaBFPx5c8KIW95Eu8ShWE00VVC3oA4emoZ2nrzBXLrUScifY6VaYYkkR
2O2tSqU8Ri3XRdgpNPDWp8ZL49KhYGYo3R/k98gnMHiY5g==
-----END CERTIFICATE-----"""

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
        val hostname = "sha256.badssl.com"

        val addr = TcpSocketAddress.resolve(hostname, 443)
        val tlsStream = SynchronousTlsStream.connect(it, CLIENT_CONTEXT, addr)

        tlsStream.writeAll(b("GET / HTTP/1.1\r\nhost:$hostname\r\n\r\n"))
        val result = tlsStream.readUpTo(2048)
        assertNotNull(result)
        assertTrue(result.startsWith(b("HTTP/1.1 200 OK")))
    }

    /**
     * Tests getting the subject for a peer certificate.
     */
    @Test
    public fun `Test getpeercert`() = ClosingScope {
        val hostname = "sha256.badssl.com"

        val addr = TcpSocketAddress.resolve(hostname, 443)
        val tlsStream = SynchronousTlsStream.connect(it, CLIENT_CONTEXT, addr)
        val cert = tlsStream.tls.peerCertificate
        assertNotNull(cert)
        val subj = cert.subject.toMap()
        assertEquals("*.badssl.com", subj["commonName"])
    }

    /**
     * Ensures an invalid TLS version is not connected to.
     */
    @Test
    public fun `Test invalid version`() {
        val addr = TcpSocketAddress.resolve("tls-v1-1.badssl.com", 1011)
        assertFailsWith<TlsException>("didn't fail to connect to TLS 1.1") {
            SynchronousTlsStream.connect(CLIENT_CONTEXT, addr) {}
        }
    }

    /**
     * Ensures a bad CA is not connected to.
     */
    @Test
    public fun `Test bad CA`() {
        val addr = TcpSocketAddress.resolve("untrusted-root.badssl.com", 443)
        assertFailsWith<TlsException>("didn't fail to connect to an untrusted root") {
            SynchronousTlsStream.connect(CLIENT_CONTEXT, addr) {}
        }
    }

    /**
     * Ensures that a bad CA that is explicitly added to the trust store is connected to.
     */
    @Test
    public fun `Test adding CA as trusted`() = ClosingScope {
        val config = TlsClientConfig()
        config.addTrustedCertificate(BADSSL_UNTRUSTED)
        val context = TlsContext(config)

        val addr = TcpSocketAddress.resolve("untrusted-root.badssl.com", 443)
        val conn = SynchronousTlsStream.connect(it, context, addr)
        val cert = conn.tls.peerCertificate
        assertNotNull(cert)
        val subj = cert.subject.toMap()
        assertEquals("*.badssl.com", subj["commonName"])
    }

    /**
     * Ensures insecure cipher connections fail.
     */
    @Test
    public fun `Test insecure connection fails`() = ClosingScope {
        val badSites = listOf(
            "cbc", "rc4-md5", "rc4", "3des", "null",
            "dh480", "dh512"
        )
        for (bad in badSites) {
            val addr = TcpSocketAddress.resolve("$bad.badssl.com", 443)
            val failure = assertFailsWith<TlsException>("didn't fail to connect to $bad") {
                SynchronousTlsStream.connect(CLIENT_CONTEXT, addr) {}
            }
            println(failure.message)
        }
    }
}
