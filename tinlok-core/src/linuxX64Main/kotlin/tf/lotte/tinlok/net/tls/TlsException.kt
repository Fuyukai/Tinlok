/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import external.openssl.ERR_error_string_n
import external.openssl.ERR_get_error
import kotlinx.cinterop.*

// TODO: Not sure if this should be a thing.
@OptIn(ExperimentalUnsignedTypes::class)
internal fun getErrorChain(): TlsException? {
    var lastError: TlsException? = null
    while (true) {
        val err = ERR_get_error()
        if (err == 0UL) return lastError
        val buf = ByteArray(256)
        buf.usePinned {
            ERR_error_string_n(err, it.addressOf(0), buf.size.convert())
        }
        val exc = TlsException(buf.toKString(), lastError)
        lastError = exc
    }
}

/**
 * Throws a new TLS exception from the error queue.
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal fun tlsError(): Nothing {
    throw getErrorChain() ?: throw IllegalArgumentException("No TLS error was on the queue!")
}

internal inline fun tlsError(block: () -> Int) {
    if (block() != 1) tlsError()
}
