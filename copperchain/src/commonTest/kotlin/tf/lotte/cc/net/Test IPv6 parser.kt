/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("RemoveRedundantBackticks")

package tf.lotte.cc.net

import tf.lotte.cc.types.ByteString
import tf.lotte.cc.types.toByteString
import kotlin.test.*

private typealias ParseException = IllegalStateException

/**
 * Tests the IPv6 parser object.
 */
public class `Test_IPv6Parser` {
    private fun baOf(vararg i: Int): ByteString {
        return intArrayOf(*i).map { it.toByte() }.toByteString()
    }

    private inline fun assertBadParse(address: String): Unit {
        assertFailsWith<ParseException> { IPv6TextParser.parse(address) }
    }

    val yert = baOf(42, 3, 176, 192, 0, 3, 0, 208, 0, 0, 0, 0, 94, 184, 192, 1)

    // == Correct tests == //
    /**
     * Tests parsing a simple valid address, with all groups and all four octets.
     */
    @Test
    public fun `Test_parsing_simple_address`() {
        val parsed = IPv6TextParser.parse("2a03:b0c0:0003:00d0:0000:0000:5eb8:c001")
        val outout = parsed.toByteString()
        assertEquals(yert, outout)
    }

    /**
     * Tests parsing an address with reduced octets.
     */
    @Test
    public fun `Test_parsing_with_reduced_octets`() {
        val parsed = IPv6TextParser.parse("2a03:b0c0:3:d0:0:0:5eb8:c001")
        val outout = parsed.toByteString()
        assertEquals(yert, outout)
    }

    /**
     * Tests parsing an address with a double colon.
     */
    @Test
    public fun `Test_parsing_with_double_colon`() {
        val parsed = IPv6TextParser.parse("2a03:b0c0:3:d0::5eb8:c001")
        val outout = parsed.toByteString()
        assertEquals(yert, outout)
    }

    /**
     * Tests basic bracketed parsing.
     */
    @Test
    public fun `Test_bracketed_parsing_no_extras`() {
        val parsed = IPv6TextParser.parse("[2a03:b0c0:3:d0::5eb8:c001]")
        val output = parsed.toByteString()
        assertEquals(yert, output)
    }

    /**
     * Tests bracketed parsing with extra data.
     */
    @Test
    public fun `Test_bracketed_parsing_with_extras`() {
        val parsed = IPv6TextParser.parse("[2a03:b0c0:3:d0::5eb8:c001]:8080")
        val output = parsed.toByteString()
        assertEquals(yert, output)
    }

    /**
     * Tests parsing only the double colon.
     */
    @Test
    public fun `Test_nil_parsing`() {
        val parsed = IPv6TextParser.parse("::")
        val output = parsed.toByteString()
        assertTrue(output.all { it == 0.toByte() })
    }

    // == Erroring tests == //
    // = Brackets = //

    /** Ensures double left-brackets fail. */
    @Test
    public fun `Test_bad_double_opening_brackets`() = assertBadParse("[[")

    /** Ensures a left-bracket in a bad position fails. */
    @Test
    public fun `Test_bad_opening_bracket_position`() = assertBadParse("0[")

    /** Ensures a standalone right-bracket fails. */
    @Test
    public fun `Test_bad_standalone_closing_bracket`() = assertBadParse("]")

    /** Ensures a right-bracket after a left-bracket fails */
    @Test
    public fun `Test_bad_right_bracket_after_left`() = assertBadParse("[]")

    /** Ensures a right-bracket after a colon fails. */
    @Test
    public fun `Test_bad_wrong_position_closing_bracket`() = assertBadParse("[0:]")

    // = Colons = //
    /** Ensures an address with less colons fails. */
    @Test
    public fun `Test_bad_too_little_colons`() = assertBadParse("0:1:2")

    /** Ensures an address with more colons fails. */
    @Test
    public fun `Test_bad_too_many_colons`() = assertBadParse("0:1:2:3:4:5:6:7:8:9")

    /** Ensures an address with trailing single colons fails. */
    @Test
    public fun `Test_bad_trailing_colon`() {
        // first colon is a separate state
        assertBadParse(":")

        // other trailing
        assertBadParse("0:")
    }

    /** Ensures an address with leading colons fails. */
    @Test
    public fun `Test_bad_leading_colon`() = assertBadParse(":0")

    /** Ensures multiple double-colons fails. */
    @Test
    public fun `Test_bad_extra_double_colon`() = assertBadParse("0::1::2")

    /** Ensures triple colon parsing fails. */
    @Test
    public fun `Test_bad_triple_colon`() {
        assertBadParse("0:::")
        assertBadParse(":::")
    }

    // = Hex digits = //
    /** Ensures an incorrect hex digit doesn't decode. */
    @Test
    public fun `Test_bad_hex_digit`() = assertBadParse("I::")

    /** Ensures an octet with too many digits doesn't parse. */
    @Test
    public fun `Test_too_many_hex_digits`() = assertBadParse("01234::")

}
