/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.tls.x509

import external.openssl.*
import kotlinx.cinterop.*
import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.net.tls.x509.X509Certificate.Companion.fromPEM
import tf.veriny.tinlok.system.toKStringUtf8Fast
import tf.veriny.tinlok.util.AtomicSafeCloseable
import tf.veriny.tinlok.util.Closeable

// https://zakird.com/2013/10/13/certificate-parsing-with-openssl

/**
 * A public-key certificate that is in the X.509 format. This is the certificate format used for
 * public TLS certificates, for example. A certificate contains certain standard attribute, which
 * are exposed via various properties, as well as some non-standard extensions, some of which are
 * exposed via properties too.
 *
 * Creating new instances of this class directly is not allowed; use the helper methods on the
 * companion object to parse and load certificates. A certificate can be loaded from a PEM-encoded
 * string with [fromPEM].
 */
public actual class X509Certificate internal constructor(
    /** Underlying handle to the OpenSSL X509 struct. */
    internal val handle: CPointer<X509>,
    /** if we own the handle */
    private val weOwnHandle: Boolean,
) : Closeable, AtomicSafeCloseable() {

    public actual companion object {
        /**
         * Actual routine to loadd a certificate from a PEM file. Used variously.
         */
        @Unsafe
        internal fun loadFromPEM(pem: String): CPointer<X509> = memScoped {
            val bio = BIO_new(BIO_s_mem())
            defer { BIO_free(bio) }
            val pemStr = pem.cstr
            BIO_write(bio, pemStr, pemStr.size)

            return PEM_read_bio_X509(bio, null, null, null)
                ?: error("Failed to create X509 certificate")
        }

        /**
         * Creates an [X509Certificate] from a PEM-encoded certificate.
         */
        @Unsafe
        public actual fun fromPEM(pem: String): X509Certificate {
            val x509 = loadFromPEM(pem)
            return X509Certificate(x509, true)
        }

        /**
         * Gets the [X509Certificate] from an [SSL] struct.
         */
        public fun fromSSL(ssl: CPointer<SSL>): X509Certificate? {
            val cert = SSL_get0_peer_certificate(ssl) ?: return null
            return X509Certificate(cert, false)
        }
    }

    override fun closeImpl() {
        if (weOwnHandle) X509_free(handle)
    }

    /**
     * The X.509 version of this certificate. See 4.1.2.1.
     */
    public actual val version: Long
        get() {
            checkOpen()

            return X509_get_version(handle)
        }

    /**
     * The serial number of this certificate. This can be any arbitrary number, so this is a String
     * (at least for now).
     */
    @OptIn(Unsafe::class)
    public actual val serial: String
        get() = memScoped {
            checkOpen()

            // Ew, yuck, gross!
            val i = X509_get_serialNumber(handle) ?: error("Failed to get serial number?")
            val bn = ASN1_INTEGER_to_BN(i, null) ?: error("Failed to convert serial to bignum")
            defer { BN_free(bn) }
            // temporary char array returned
            val tmp = BN_bn2dec(bn) ?: error("Failed to convert bignum to decimal")
            defer { K_OPENSSL_free(tmp) }
            return tmp.toKStringUtf8Fast()
        }


    /** The entity that has signed and issued this certificate. */
    @OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
    public actual val issuer: List<Pair<String, String>>
        get() {
            checkOpen()

            // an X509_name has multiple entries, which we iterate over and return a "mapping" of
            val name = X509_get_issuer_name(handle) ?: error("Failed to get issuer name")
            return name.toPairs()
        }

    /** The entity this certificate was issued for. */
    @OptIn(Unsafe::class)
    public actual val subject: List<Pair<String, String>>
        get() {
            checkOpen()

            // similar deal here.
            val name = X509_get_subject_name(handle) ?: error("Failed to get subject name")
            return name.toPairs()
        }

    /** If this certificate is a CA. */
    public actual val isCertificateAuthority: Boolean
        get() {
            checkOpen()

            return X509_check_ca(handle) != 0
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is X509Certificate) return false
        if (!_isOpen) return false  // meaningless

        val res = X509_cmp(handle, other.handle)
        return res == 0
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private val hashCode by lazy {
        memScoped {
            checkOpen()

            // we use the standard x509 digest as the hash code. not the "best", but oh well.
            val md = EVP_blake2b512() ?: error("failed to get message digest")
            val buf = ByteArray(64)  // we know what the size will be for blake2b
            val outSize = alloc<UIntVar>()
            val res = buf.usePinned {
                val ptr = it.addressOf(0).reinterpret<UByteVar>()
                X509_digest(handle, md, ptr, outSize.ptr)
            }
            if (res != 1) error("X509_digest failed")

            assert(outSize.value == 64U)
            buf.contentHashCode()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hashCode(): Int {
        return hashCode
    }

    override fun toString(): String {
        return "<X509Certificate CN='${commonName}'>"
    }

    /**
     * The signature algorithm used for this certificate's signature. This is named (confusingly)
     * just ``signature`` in RFC 5280.
     */
    public actual val signatureAlgorithm: String get() = TODO()
}
