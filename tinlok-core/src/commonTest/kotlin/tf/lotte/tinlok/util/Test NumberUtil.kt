/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("PrivatePropertyName", "ClassName")

package tf.lotte.tinlok.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests various number util functions.
 */
public class `Test NumberUtil` {
    private val INT_TEST = byteArrayOf(0x0f, 0x12, 0x34, 0x56)
    private val LONG_TEST = byteArrayOf(0x00, 0x12, 0x34, 0x56, 0x78, 0x01, 0x23, 0x45)

    @Test
    public fun `Test Int toByteArray`() {
        val i = 0x0f_12_34_56
        val ba = i.toByteArray()
        assertTrue(
            ba.contentEquals(INT_TEST),
            "Expected ${INT_TEST.contentToString()}, got ${ba.contentToString()}"
        )
    }

    @Test
    public fun `Test ByteArray toInt`() {
        val i = INT_TEST.toInt()
        assertEquals(0x0f_12_34_56, i)
    }

    @Test
    public fun `Test Long toByteArray`() {
        val i = 0x00_12_34_56_78_01_23_45L
        val ba = i.toByteArray()
        // 0x99 is not a byte?
        assertTrue(
            ba.contentEquals(LONG_TEST),
            "Expected ${LONG_TEST.contentToString()}, got ${ba.contentToString()}"
        )
    }

    @Test
    public fun `Test ByteArray toLong`() {
        val i = LONG_TEST.toLong()
        assertEquals(0x00_12_34_56_78_01_23_45L, i)
    }
}
