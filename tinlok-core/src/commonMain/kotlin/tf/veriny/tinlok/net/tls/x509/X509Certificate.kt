/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.tls.x509

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.net.tls.x509.X509Certificate.Companion.fromPEM
import tf.veriny.tinlok.util.Closeable

/**
 * A public-key certificate that is in the X.509 format. This is the certificate format used for
 * public TLS certificates, for example. A certificate contains certain standard attribute, which
 * are exposed via various properties, as well as some non-standard extensions, some of which are
 * exposed via properties too.
 *
 * Creating new instances of this class directly is not allowed; use the helper methods on the
 * companion object to parse and load certificates. A certificate can be loaded from a PEM-encoded
 * string with [fromPEM], or from a raw byte stream using [fromRaw].
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class X509Certificate : Closeable {
    public companion object {
        /**
         * Creates an [X509Certificate] from a PEM-encoded certificate.
         */
        @Unsafe
        public fun fromPEM(pem: String): X509Certificate
    }

    /** The X.509 version of this certificate. See 4.1.2.1. */
    public val version: Long

    /**
     * The serial number of this certificate. This can be any arbitrary number, so this is a String
     * (at least for now).
     */
    public val serial: String

    /**
     * The signature algorithm used for this certificate's signature. This is named (confusingly)
     * just ``signature`` in RFC 5280.
     */
    public val signatureAlgorithm: String

    /** The entity that has signed and issued this certificate. */
    public val issuer: List<Pair<String, String>>

    /** The subject this signature was issued for. */
    public val subject: List<Pair<String, String>>

    /** Checks if this certificate is a CA (i.e. it can issue new certificates. */
    public val isCertificateAuthority: Boolean
}

/**
 * Gets the common name for this certificate.
 */
public val X509Certificate.commonName: String?
    get() = subject.find { it.first == "commonName" }?.second
