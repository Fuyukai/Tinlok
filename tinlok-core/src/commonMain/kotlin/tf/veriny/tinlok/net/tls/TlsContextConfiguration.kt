/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.tls

import tf.veriny.tinlok.net.tls.x509.X509Certificate

/**
 * Sealed class for the possible types of TLS connection configuration.
 */
public sealed class TlsContextConfiguration(
    /**
     * The set of valid TLS protocols to use for the connection. By default, this is TLS 1.2 and
     * TLS 1.3.
     */
    public open val versions: Set<TlsVersion>,

    /**
     * The set of ALPN protocols to use. See RFC 8447 for more information.
     */
    public val alpnProtocols: MutableSet<String>,

    /**
     * If compatibility ciphers should be used over the most secure ciphers.
     */
    public val compatibilityCiphers: Boolean,
)

/**
 * The client-side configuration for a TLS context.
 */
public class TlsClientConfig(
    versions: Set<TlsVersion> = setOf(TlsVersion.TLS_V12, TlsVersion.TLS_V13),
    alpnProtocols: MutableSet<String> = mutableSetOf(),
    compatibilityCiphers: Boolean = false,
) : TlsContextConfiguration(versions, alpnProtocols, compatibilityCiphers) {
    /** The list of extra certificates to be added to the X509_STORE. */
    internal val extraCertificates = mutableListOf<String>()

    /**
     * Adds a new PEM certificate to the trust store. Any certificates signed by this one will be
     * considered verified if sent by a server.
     */
    public fun addTrustedCertificate(pem: String) {
        extraCertificates.add(pem)
    }
}

public class TlsServerConfig(
    /** The PEM-encoded certificate to offer as this server's certificate. */
    public val certificatePem: String,
    /** The PEM-encoded private key for the server certificate. */
    public val privateKeyPem: String,
    public val chain: List<X509Certificate> = listOf(),
    versions: Set<TlsVersion> = setOf(TlsVersion.TLS_V12, TlsVersion.TLS_V13),
    alpnProtocols: MutableSet<String> = mutableSetOf(),
    compatibilityCiphers: Boolean = false,
) : TlsContextConfiguration(versions, alpnProtocols, compatibilityCiphers) {

}
