/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.codec

import tf.lotte.tinlok.util.b
import kotlin.test.Test
import kotlin.test.assertEquals

class `Test Base64`() {
    // standard multiple of three input
    @Test
    fun `Test aligned encoding`() {
        val to = b("abcdef")
        val encoded = base64Encode(to)
        assertEquals("YWJjZGVm", encoded)
    }

    // input that only has two bytes at the end
    @Test
    fun `Test two-block encoding`() {
        val to = b("abcde")
        val encoded = base64Encode(to)
        assertEquals("YWJjZGU=", encoded)
    }

    // input with only one byte at the end
    @Test
    fun `Test one-block encoding`() {
        val to = b("abcd")
        val encoded = base64Encode(to)
        assertEquals(encoded, "YWJjZA==")
    }

    // standard multiple of four input
    @Test
    fun `Test aligned decoding`() {
        val output = base64Decode("YWJjZGVm")
        assertEquals(b("abcdef"), output)
    }

    // input with a padding byte
    @Test
    fun `Test two-block decoding`() {
        val output = base64Decode("YWJjZGU=")
        assertEquals(b("abcde"), output)
    }

    @Test
    fun `Test one-block decoding`() {
        val output = base64Decode("YWJjZA==")
        assertEquals(b("abcd"), output)
    }
}
