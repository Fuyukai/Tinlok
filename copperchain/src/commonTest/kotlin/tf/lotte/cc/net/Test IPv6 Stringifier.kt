/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.net

import tf.lotte.cc.Unsafe
import tf.lotte.cc.util.baOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the stringification of IPv6 addresses.
 */
@Suppress("RemoveRedundantBackticks")
public class `Test_IPv6_Stringifier` {
    val yert = baOf(42, 3, 176, 192, 0, 3, 0, 208, 0, 0, 0, 0, 94, 184, 192, 1)
    val stringifier = IPv6Stringifier(yert.unwrapCopy())

    // not really much here, this one test will always be correct
    /**
     * Tests stringification in canonical representation.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test_canonical_stringification`() {
        assertEquals("2a03:b0c0:0003:00d0:0000:0000:5eb8:c001", stringifier.canonical())
    }

    /**
     * Tests correct stringification on an example address.
     */
    @Test
    public fun `Test_correct_stringification_yert`() {
        val correct = stringifier.correct()
        assertEquals("2a03:b0c0:3:d0::5eb8:c001", correct)
    }

    /**
     * Tests correct stringification on an address with a leading run of zeroes.
     */
    @Test
    public fun `Test_correct_stringification_starting_zeroes`() {
        val parsed = IPv6TextParser.parse("::1:2:3")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("::1:2:3", correct)
    }

    /**
     * Tests correct stringification on an address with a trailing run of zeroes.
     */
    @Test
    public fun `Test_correct_stringification_ending_zeroes`() {
        val parsed = IPv6TextParser.parse("1:2:3::")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("1:2:3::", correct)
    }

    /**
     * Tests correct stringification on an address with two possible runs of zero.
     */
    @Test
    public fun `Test_correct_stringification_two_runs`() {
        val parsed = IPv6TextParser.parse("1:00:00:00:1:00:00:00")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("1::1:0:0:0", correct)
    }

    /**
     * Tests correct stringification on an address with no runs of zero.
     */
    @Test
    public fun `Test_correct_stringification_no_runs`() {
        val parsed = IPv6TextParser.parse("00:01:00:03:00:05:00:07")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("0:1:0:3:0:5:0:7", correct)
    }
}
