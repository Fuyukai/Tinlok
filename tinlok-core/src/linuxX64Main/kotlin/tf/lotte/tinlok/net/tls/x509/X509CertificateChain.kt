/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls.x509

import external.openssl.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.tls.tlsError
import tf.lotte.tinlok.util.AtomicSafeCloseable
import tf.lotte.tinlok.util.Closeable

/**
 * A chain of X509 certificates, each one higher signing a certificate lower in the chain. A chain
 * may be ordered or unordered; make no assumptions as such.
 *
 * This class is designed to be passed to a TlsContext, rather than used directly.
 */
public actual class X509CertificateChain internal constructor(
    ptrs: List<CPointer<X509>>,
) : Closeable, AtomicSafeCloseable(), Iterable<X509Certificate> {
    public actual companion object {
        /**
         * Creates a new [X509CertificateChain] from the PEM string specified.
         */
        @Unsafe
        public actual fun fromPEM(s: String): X509CertificateChain = memScoped {
            val bio = BIO_new(BIO_s_mem()) ?: tlsError()
            defer { BIO_free(bio) }
            val pemStr = s.cstr
            BIO_write(bio, pemStr, pemStr.size)
            val ptrs = mutableListOf<CPointer<X509>>()

            // reading ssl_rsa.c, SSL_CTX_use_certificate_chain_file, what openssl seems to do is
            // just read from the BIO repeatedly until it returns null.
            // this is also what we will do.
            while (true) {
                // null == failed to read, in some manner
                val ptr = PEM_read_bio_X509(bio, null, null, null) ?: break
                ptrs.add(ptr)
            }

            if (ptrs.isEmpty()) {
                throw IllegalArgumentException(
                    "Input string did not contain any valid certificates"
                )
            }

            return X509CertificateChain(ptrs)
        }
    }

    /** The list of certificates that this chain has. */
    public actual val certificates: List<X509Certificate> = ptrs.map { X509Certificate(it, true) }

    override fun closeImpl() {
        certificates.map { it.close() }
    }

    override fun iterator(): Iterator<X509Certificate> {
        return certificates.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is X509CertificateChain) return false
        return other.certificates == certificates
    }

}
