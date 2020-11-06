/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.cc.net.IPv4Address
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests IPv4 parsing/string representations.
 */
class `Test IPv4Address` {
    @Test
    fun `Test string parsing`() {
        // simple incoming parsing
        val addr = IPv4Address.of("39.3.9.0")
        assertEquals(addr.toString(), "39.3.9.0")

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun `Test decimal parsing`() {
        val addr = IPv4Address.of(0x7F000001U)
        assertEquals(addr.toString(), "127.0.0.1")
    }

    @Test
    fun `Test failure parsing`() {
        // not any kind of ip address
        assertFailsWith<IllegalArgumentException> {
            IPv4Address.of("zzzzzz")
        }

        // not enough dots
        assertFailsWith<IllegalArgumentException> {
            IPv4Address.of("1.1.1")
        }

        // too MANY dots
        assertFailsWith<IllegalArgumentException> {
            IPv4Address.of("1.1.1.1.1")
        }

        // empty dots
        assertFailsWith<IllegalArgumentException> {
            IPv4Address.of("1..1.1")
        }

        // out of range
        assertFailsWith<IllegalArgumentException> {
            IPv4Address.of("300.300.300.300")
        }
    }
}
