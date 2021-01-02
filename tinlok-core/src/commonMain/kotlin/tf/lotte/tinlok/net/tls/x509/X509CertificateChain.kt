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
import tf.lotte.tinlok.util.Closeable

/**
 * A chain of X509 certificates, each one higher signing a certificate lower in the chain. A chain
 * may be ordered or unordered; make no assumptions as such.
 *
 * This class is designed to be passed to a TlsContext, rather than used directly.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class X509CertificateChain : Closeable, Iterable<X509Certificate> {
    public companion object {
        /**
         * Creates a new [X509CertificateChain] from the PEM string specified.
         */
        @Unsafe
        public fun fromPEM(s: String): X509CertificateChain
    }

    /**
     * The list of certificates that this chain has.
     *
     * These certificates can be safely used without needing to close them, as their lifetime is
     * managed by this object.
     */
    public val certificates: List<X509Certificate>
}
