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
 * Tests the X509CertificateChain class.
 */
public class `Test X509CertificateChain` {
    public companion object {
        // A valid certificate chain (for StackExchange!)
        public const val VALID_CHAIN = """-----BEGIN CERTIFICATE-----
MIIHJTCCBg2gAwIBAgISA/c80WOrBS1B0YKU1WnbOIwuMA0GCSqGSIb3DQEBCwUA
MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD
ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0yMDEwMDUxMzAyNDRaFw0y
MTAxMDMxMzAyNDRaMB4xHDAaBgNVBAMMEyouc3RhY2tleGNoYW5nZS5jb20wggEi
MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDgvEf4788HVB81wIAnFbY556Qb
7BOB5IhjozLwLS9OsOAn2Dmr+P/456nysCXQAFw/Y98R+INfjTScScZa+WfKM9tk
TSLrrHuPyFQ0IEwpy59+cdnPoJQWrAu6Y0RGRv27yOOVRyeAqge2pArDiYqrc0sE
HSrBSS1wsq/nnzcaSZroL9uBqGi8hhe5GJUYk2F5EiexsYxv9jx8uLQ7vpBmk3Et
JbOlP00unQZH5Wd6swTntOhFUHSE2g3Bj3Wi/Mjhq6spTQmvjazN6+ZT6l+UEFSI
8PdlS9cH99DlPyVxiZfezobk9CGAfkhWhFRoecXKIeMGY49jUmicuZJfa5A7AgMB
AAGjggQvMIIEKzAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEG
CCsGAQUFBwMCMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFK+7kfNW1XVWKaiJnPL+
LA+dQ6qqMB8GA1UdIwQYMBaAFKhKamMEfd265tE5t6ZFZe/zqOyhMG8GCCsGAQUF
BwEBBGMwYTAuBggrBgEFBQcwAYYiaHR0cDovL29jc3AuaW50LXgzLmxldHNlbmNy
eXB0Lm9yZzAvBggrBgEFBQcwAoYjaHR0cDovL2NlcnQuaW50LXgzLmxldHNlbmNy
eXB0Lm9yZy8wggHkBgNVHREEggHbMIIB14IPKi5hc2t1YnVudHUuY29tghIqLmJs
b2dvdmVyZmxvdy5jb22CEioubWF0aG92ZXJmbG93Lm5ldIIYKi5tZXRhLnN0YWNr
ZXhjaGFuZ2UuY29tghgqLm1ldGEuc3RhY2tvdmVyZmxvdy5jb22CESouc2VydmVy
ZmF1bHQuY29tgg0qLnNzdGF0aWMubmV0ghMqLnN0YWNrZXhjaGFuZ2UuY29tghMq
LnN0YWNrb3ZlcmZsb3cuY29tghUqLnN0YWNrb3ZlcmZsb3cuZW1haWyCDyouc3Vw
ZXJ1c2VyLmNvbYINYXNrdWJ1bnR1LmNvbYIQYmxvZ292ZXJmbG93LmNvbYIQbWF0
aG92ZXJmbG93Lm5ldIIUb3BlbmlkLnN0YWNrYXV0aC5jb22CD3NlcnZlcmZhdWx0
LmNvbYILc3N0YXRpYy5uZXSCDXN0YWNrYXBwcy5jb22CDXN0YWNrYXV0aC5jb22C
EXN0YWNrZXhjaGFuZ2UuY29tghJzdGFja292ZXJmbG93LmJsb2eCEXN0YWNrb3Zl
cmZsb3cuY29tghNzdGFja292ZXJmbG93LmVtYWlsghFzdGFja3NuaXBwZXRzLm5l
dIINc3VwZXJ1c2VyLmNvbTBMBgNVHSAERTBDMAgGBmeBDAECATA3BgsrBgEEAYLf
EwEBATAoMCYGCCsGAQUFBwIBFhpodHRwOi8vY3BzLmxldHNlbmNyeXB0Lm9yZzCC
AQMGCisGAQQB1nkCBAIEgfQEgfEA7wB1AJQgvB6O1Y1siHMfgosiLA3R2k1ebE+U
PWHbTi9YTaLCAAABdPkSXP4AAAQDAEYwRAIgVay70Cu9d46NEOmUt3XUu7bXIqkS
h+DQXw0Rdy5qIQ0CIH4GmNouXeCovRlx/T4B9Hh//+VvA1tBakgiq+6WEPR8AHYA
fT7y+I//iFVoJMLAyp5SiXkrxQ54CX8uapdomX4i8NcAAAF0+RJdVgAABAMARzBF
AiEAs4iZyvg1zC2zaFCs9CNuiGhkuD3cdmcuPCx1qi7rZqcCIAQIaxcyd5wkVWNj
1CeXrUriThrMyOElkNXaN34j3WqUMA0GCSqGSIb3DQEBCwUAA4IBAQA5BQYZcDBu
h1NnUYspMTFcuDjYSmZDlD9MBTSaA4alsHN2l+jsz/cLgPNZWdOhn1NPb6OU3x4J
AOz/4waQvqQ0VYhjBplLMiH3HPXHIiaHJw+p+Hdz0gi3gMcvuoz7ifu+9GemmdGV
wdpeGuZP4NQXJCnuNhwjrqFQHuoimKvm2M555fJB+ij+p3K2KhbQnq2BKnn2EqIR
OX9Euhv1TVpUz+rSSJJ89tIUAqzpHSS6CJt3Z3Ljgtyy1u0J1+UNlJ69JNEZIhsG
fcfc6rV6/wF3uRRBdJck9qyMCejg7NESyxTGnj+QcgbzEpMbGdzZ0PCyvaJWccl7
qysRzGiJF1WI
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
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
-----END CERTIFICATE-----
"""
    }

    /**
     * Tests parsing a valid certificate chain.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test parsing valid chain`() = ClosingScope {
        val chain = X509CertificateChain.fromPEM(VALID_CHAIN)
        it.add(chain)

        assertEquals(2, chain.certificates.size)
        val se = chain.certificates[0]
        assertEquals("*.stackexchange.com", se.commonName)
        val issuer = se.issuer.toMap()
        assertEquals("Let's Encrypt Authority X3", issuer["commonName"])

        val le = chain.certificates[1]
        assertEquals(le.commonName, "Let's Encrypt Authority X3")
    }
}
