/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("ReplaceToWithInfixForm")

package tf.lotte.tinlok.net.tls.x509

import external.openssl.*
import kotlinx.cinterop.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.system.readBytesFast


/**
 * Converts an ASN1_OBJECT to a string.
 */
@Unsafe
private fun CPointer<ASN1_OBJECT>.toKString(): String {
    var buflen = OBJ_obj2txt(null, 0, this, 0 /* Use short/long names */)
    if (buflen == NID_undef) {
        error("OBJ_obj2txt size call failed")
    }

    // +1 for trailing null? idk.
    buflen += 1

    val buffer = ByteArray(buflen)
    val res = buffer.usePinned {
        OBJ_obj2txt(it.addressOf(0), buflen, this, 0)
    }
    if (res == NID_undef) {
        error("OBJ_obj2txt failed")
    }
    return buffer.toKString(endIndex = buffer.size - 1)
}

/**
 * Converts an ASN1_STRING to a Kotlin string.
 */
@Unsafe
private fun CPointer<ASN1_STRING>.toKString(): String = memScoped {
    // double pointers...
    // I really hate how confusing double pointers are in K/N
    val buf = allocPointerTo<UByteVar>()
    val res = ASN1_STRING_to_UTF8(buf.ptr, this@toKString)
    if (res < 0 || buf.pointed == null) {
        error("ASN1_STRING_to_UTF8 failed")
    }
    defer { K_OPENSSL_free(buf.value) }

    val out = buf.value!!.reinterpret<ByteVar>()
    return out.readBytesFast(res).decodeToString()
}

/**
 * Turns an X509_NAME_ENTRY into a pair of (name, value).
 */
@Unsafe
private fun CPointer<X509_NAME_ENTRY>.toPair(): Pair<String, String> {
    val field = X509_NAME_ENTRY_get_object(this)
        ?: error("entry had no object (name)?")
    val value = X509_NAME_ENTRY_get_data(this)
        ?: error("entry had no data (value)?")

    val fieldStr = field.toKString()
    val valueStr = value.toKString()
    return fieldStr.to(valueStr)
}

/**
 * Turns an X509_name struct into a list of (name, value) pairs.
 */
@Unsafe
internal fun CPointer<X509_NAME>.toPairs(): List<Pair<String, String>> {
    // X509_NAME structs contain a list of X509_NAME_ENTRY structs
    // so we take them from this struct, turn them into a pair using toPair()
    val count = X509_NAME_entry_count(this)

    return (0 until count)
        .map { X509_NAME_get_entry(this, it) ?: error("X509_name_get_entry($it) failed") }
        .map { it.toPair() }
}
