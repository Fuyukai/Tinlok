/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls.x509

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.util.ClosingScope
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the X509 certificate class.
 */
public class `Test X509 Certificate` {
    companion object {
        private const val CERTIFICATE = """-----BEGIN CERTIFICATE-----
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
    }

    private fun ensureMap(map: Map<String, String>) {
        val country = map["countryName"]
        assertEquals("AB", country)
        val state = map["stateOrProvinceName"]
        assertEquals("Some State", state)
        val locality = map["localityName"]
        assertEquals("Some City", locality)
        val organisation = map["organizationName"]
        assertEquals("Some Company", organisation)
        val commonName = map["commonName"]
        assertEquals("some.domain.name", commonName)
        val email = map["emailAddress"]
        assertEquals("some@email.com", email)
    }

    /**
     * Tests the serial number property.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test serial`() = ClosingScope {
        val cert = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert)

        assertEquals("286099426827341504532947095099215708059461209693", cert.serial)
    }

    /**
     * Tests the issuer dictionary.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test issuer`() = ClosingScope {
        val cert = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert)

        // known safe as the certificate above has no duplicates
        val issuer = cert.issuer.toMap()
        ensureMap(issuer)
    }

    /**
     * Tests the subject dictionary.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test subject`() = ClosingScope {
        val cert = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert)

        // this is the exact same as the issuer dict, as this is self-signed.
        val subject = cert.issuer.toMap()
        ensureMap(subject)
    }
}
