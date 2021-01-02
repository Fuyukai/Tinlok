/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("RemoveRedundantBackticks")

package tf.veriny.tinlok.net

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.util.ByteString
import tf.veriny.tinlok.util.toByteString
import kotlin.test.*

/**
 * Creates a new [ByteString] from the specified [i] ints. This is better than byteArrayOf as it
 * supports >128 <=255 literals.
 */
public fun baOf(vararg i: Int): ByteString {
    return intArrayOf(*i).map { it.toByte() }.toByteString()
}


/**
 * Tests the IPv6 parser object.
 */
public class `Test IPv6Parser` {
    private inline fun assertBadParse(address: String): Unit {
        assertFailsWith<IPv6ParseException> { IPv6TextParser.parse(address) }
    }

    val yert = baOf(42, 3, 176, 192, 0, 3, 0, 208, 0, 0, 0, 0, 94, 184, 192, 1)

    // == Correct tests == //
    /**
     * Tests parsing a simple valid address, with all groups and all four octets.
     */
    @Test
    public fun `Test parsing simple address`() {
        val parsed = IPv6TextParser.parse("2a03:b0c0:0003:00d0:0000:0000:5eb8:c001")
        val outout = parsed.toByteString()
        assertEquals(yert, outout)
    }

    /**
     * Tests parsing an address with reduced octets.
     */
    @Test
    public fun `Test parsing with reduced octets`() {
        val parsed = IPv6TextParser.parse("2a03:b0c0:3:d0:0:0:5eb8:c001")
        val outout = parsed.toByteString()
        assertEquals(yert, outout)
    }

    /**
     * Tests parsing an address with a double colon.
     */
    @Test
    public fun `Test parsing with double colon`() {
        val parsed = IPv6TextParser.parse("2a03:b0c0:3:d0::5eb8:c001")
        val outout = parsed.toByteString()
        assertEquals(yert, outout)
    }

    /**
     * Tests basic bracketed parsing.
     */
    @Test
    public fun `Test bracketed parsing no extras`() {
        val parsed = IPv6TextParser.parse("[2a03:b0c0:3:d0::5eb8:c001]")
        val output = parsed.toByteString()
        assertEquals(yert, output)
    }

    /**
     * Tests bracketed parsing with extra data.
     */
    @Test
    public fun `Test bracketed parsing with extras`() {
        val parsed = IPv6TextParser.parse("[2a03:b0c0:3:d0::5eb8:c001]:8080")
        val output = parsed.toByteString()
        assertEquals(yert, output)
    }

    /**
     * Tests parsing only the double colon.
     */
    @Test
    public fun `Test nil parsing`() {
        val parsed = IPv6TextParser.parse("::")
        val output = parsed.toByteString()
        assertTrue(output.all { it == 0.toByte() })
    }

    // == Erroring tests == //
    // = Brackets = //

    /** Ensures double left-brackets fail. */
    @Test
    public fun `Test bad double opening brackets`() = assertBadParse("[[")

    /** Ensures a left-bracket in a bad position fails. */
    @Test
    public fun `Test bad opening bracket position`() = assertBadParse("0[")

    /** Ensures a standalone right-bracket fails. */
    @Test
    public fun `Test bad standalone closing bracket`() = assertBadParse("]")

    /** Ensures a right-bracket after a left-bracket fails */
    @Test
    public fun `Test bad right bracket after left`() = assertBadParse("[]")

    /** Ensures a right-bracket after a colon fails. */
    @Test
    public fun `Test bad wrong position closing bracket`() = assertBadParse("[0:]")

    // = Colons = //
    /** Ensures an address with less colons fails. */
    @Test
    public fun `Test bad too little colons`() = assertBadParse("0:1:2")

    /** Ensures an address with more colons fails. */
    @Test
    public fun `Test bad too many colons`() = assertBadParse("0:1:2:3:4:5:6:7:8:9")

    /** Ensures an address with trailing single colons fails. */
    @Test
    public fun `Test bad trailing colon`() {
        // first colon is a separate state
        assertBadParse(":")

        // other trailing
        assertBadParse("0:")
    }

    /** Ensures an address with leading colons fails. */
    @Test
    public fun `Test bad leading colon`() = assertBadParse(":0")

    /** Ensures multiple double-colons fails. */
    @Test
    public fun `Test bad extra double colon`() = assertBadParse("0::1::2")

    /** Ensures triple colon parsing fails. */
    @Test
    public fun `Test bad triple colon`() {
        assertBadParse("0:::")
        assertBadParse(":::")
    }

    // = Hex digits = //
    /** Ensures an incorrect hex digit doesn't decode. */
    @Test
    public fun `Test bad hex digit`() = assertBadParse("I::")

    /** Ensures an octet with too many digits doesn't parse. */
    @Test
    public fun `Test too many hex digits`() = assertBadParse("01234::")

}

/**
 * Tests the stringification of IPv6 addresses.
 */
@Suppress("RemoveRedundantBackticks")
public class `Test IPv6 Stringifier` {
    val yert = baOf(42, 3, 176, 192, 0, 3, 0, 208, 0, 0, 0, 0, 94, 184, 192, 1)
    val stringifier = IPv6Stringifier(yert.unwrapCopy())

    // not really much here, this one test will always be correct
    /**
     * Tests stringification in canonical representation.
     */
    @OptIn(Unsafe::class)
    @Test
    public fun `Test canonical stringification`() {
        assertEquals("2a03:b0c0:0003:00d0:0000:0000:5eb8:c001", stringifier.canonical())
    }

    /**
     * Tests correct stringification on an example address.
     */
    @Test
    public fun `Test correct stringification yert`() {
        val correct = stringifier.correct()
        assertEquals("2a03:b0c0:3:d0::5eb8:c001", correct)
    }

    /**
     * Tests correct stringification on an address with a leading run of zeroes.
     */
    @Test
    public fun `Test correct stringification starting zeroes`() {
        val parsed = IPv6TextParser.parse("::1:2:3")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("::1:2:3", correct)
    }

    /**
     * Tests correct stringification on an address with a trailing run of zeroes.
     */
    @Test
    public fun `Test correct stringification ending zeroes`() {
        val parsed = IPv6TextParser.parse("1:2:3::")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("1:2:3::", correct)
    }

    /**
     * Tests correct stringification on an address with two possible runs of zero.
     */
    @Test
    public fun `Test correct stringification two runs`() {
        val parsed = IPv6TextParser.parse("1:00:00:00:1:00:00:00")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("1::1:0:0:0", correct)
    }

    /**
     * Tests correct stringification on an address with no runs of zero.
     */
    @Test
    public fun `Test correct stringification no runs`() {
        val parsed = IPv6TextParser.parse("00:01:00:03:00:05:00:07")
        val correct = IPv6Stringifier(parsed).correct()
        assertEquals("0:1:0:3:0:5:0:7", correct)
    }
}
