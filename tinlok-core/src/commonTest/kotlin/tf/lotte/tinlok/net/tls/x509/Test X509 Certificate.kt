/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
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

        private const val CERTIFICATE_2 = """-----BEGIN CERTIFICATE-----
MIIEkjCCA3qgAwIBAgIQCgFBQgAAAVOFc2oLheynCDANBgkqhkiG9w0BAQsFADA/
MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT
DkRTVCBSb290IENBIFgzMB4XDTE2MDMxNzE2NDA0NloXDTIxMDMxNzE2NDA0Nlow
SjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxIzAhBgNVBAMT
GkxldCdzIEVuY3J5cHQgQXV0aG9yaXR5IFgzMIIBIjANBgkqhkiG9w0BAQEFAAOC
AQ8AMIIBCgKCAQEAnNMM8FrlLke3cl03g7NoYzDq1zUmGSXhvb418XCSL7e4S0EF
q6meNQhY7LEqxGiHC6PjdeTm86dicbp5gWAf15Gan/PQeGdxyGkOlZHP/uaZ6WA8
SMx+yk13EiSdRxta67nsHjcAHJyse6cF6s5K671B5TaYucv9bTyWaN8jKkKQDIZ0
Z8h/pZq4UmEUEz9l6YKHy9v6Dlb2honzhT+Xhq+w3Brvaw2VFn3EK6BlspkENnWA
a6xK8xuQSXgvopZPKiAlKQTGdMDQMc2PMTiVFrqoM7hD8bEfwzB/onkxEz0tNvjj
/PIzark5McWvxI0NHWQWM6r6hCm21AvA2H3DkwIDAQABo4IBfTCCAXkwEgYDVR0T
AQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwfwYIKwYBBQUHAQEEczBxMDIG
CCsGAQUFBzABhiZodHRwOi8vaXNyZy50cnVzdGlkLm9jc3AuaWRlbnRydXN0LmNv
bTA7BggrBgEFBQcwAoYvaHR0cDovL2FwcHMuaWRlbnRydXN0LmNvbS9yb290cy9k
c3Ryb290Y2F4My5wN2MwHwYDVR0jBBgwFoAUxKexpHsscfrb4UuQdf/EFWCFiRAw
VAYDVR0gBE0wSzAIBgZngQwBAgEwPwYLKwYBBAGC3xMBAQEwMDAuBggrBgEFBQcC
ARYiaHR0cDovL2Nwcy5yb290LXgxLmxldHNlbmNyeXB0Lm9yZzA8BgNVHR8ENTAz
MDGgL6AthitodHRwOi8vY3JsLmlkZW50cnVzdC5jb20vRFNUUk9PVENBWDNDUkwu
Y3JsMB0GA1UdDgQWBBSoSmpjBH3duubRObemRWXv86jsoTANBgkqhkiG9w0BAQsF
AAOCAQEA3TPXEfNjWDjdGBX7CVW+dla5cEilaUcne8IkCJLxWh9KEik3JHRRHGJo
uM2VcGfl96S8TihRzZvoroed6ti6WqEBmtzw3Wodatg+VyOeph4EYpr/1wXKtx8/
wApIvJSwtmVi4MFU5aMqrSDE6ea73Mj2tcMyo5jMd6jmeWUHK8so/joWUoHOUgwu
X4Po1QYz+3dszkDqMp4fklxBwXRsW10KXzPMTZ+sOPAveyxindmjkW8lGy+QsRlG
PfZ+G6Z6h7mjem0Y+iWlkYcV4PIWL1iwBi8saCbGS5jN2p8M+X+Q7UNKEkROb3N6
KOqkqm57TH2H3eDJAkSnh6/DNFu0Qg==
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

    /**
     * Tests certificate equality.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test equals`() = ClosingScope {
        val cert = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert)
        val cert2 = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert2)

        assertEquals(cert2, cert)
    }

    /**
     * Tests certificate hash codes.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test hashes`() = ClosingScope {
        val cert1 = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert1)
        val cert2 = X509Certificate.fromPEM(CERTIFICATE)
        it.add(cert2)

        val set1 = mutableSetOf(cert1, cert2)
        assertEquals(1, set1.size)

        val cert3 = X509Certificate.fromPEM(CERTIFICATE_2)
        it.add(cert3)
        val set2 = mutableSetOf(cert1, cert3)
        assertEquals(2, set2.size)
    }
}
