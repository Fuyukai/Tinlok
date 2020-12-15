/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import external.openssl.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import tf.lotte.tinlok.Unsafe
import kotlin.experimental.ExperimentalTypeInference

@Unsafe
internal inline fun TlsContext.ctxerr(name: String, block: () -> Int) {
    val res = block()
    if (res == 0) {
        close()
        throw IllegalStateException("context call failed at $name")
    }
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@Unsafe
internal inline fun TlsContext.ctxerr(name: String, block: () -> Long) {
    val res = block()
    if (res == 0L) {
        close()
        throw IllegalStateException("context call failed at $name")
    }
}

@Unsafe
internal inline fun TlsObject.sslerr(name: String, block: () -> Int) {
    val res = block()
    if (res == 0) {
        close()
        throw IllegalStateException("ssl call failed at $name")
    }
}


@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@Unsafe
internal inline fun TlsObject.sslerr(name: String, block: () -> Long) {
    val res = block()
    if (res == 0L) {
        close()
        throw IllegalStateException("ssl call failed at $name")
    }
}

/**
 * Loads a private key from a PEM-encoded string.
 */
internal fun SSL_CTX_use_privatekey_pem(ctx: CPointer<SSL_CTX>, pem: String): Int = memScoped {
    val bio = BIO_new(BIO_s_mem()) ?: error("Failed to create memory BIO")
    defer { BIO_free(bio) }

    val pemStr = pem.cstr
    BIO_write(bio, pemStr, pemStr.size)
    val pkey = PEM_read_bio_PrivateKey(bio, null, null, null)
        ?: error("Failed to read private key")
    SSL_CTX_use_PrivateKey(ctx, pkey)
}
