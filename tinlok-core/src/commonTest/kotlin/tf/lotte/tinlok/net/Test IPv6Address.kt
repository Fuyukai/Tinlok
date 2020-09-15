/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the IPv6Address class.
 */
class `Test IPv6Address` {
    @Test
    fun `Test parsing`() {
        val addr = IPv6Address.of("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
        assertEquals(addr.toString(), "2001:db8:85a3::8a2e:370:7334")

        assertFailsWith<IllegalArgumentException> {
            IPv6Address.of("zzzzzz")
        }
    }
}
