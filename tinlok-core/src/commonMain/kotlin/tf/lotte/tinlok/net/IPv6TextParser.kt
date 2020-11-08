/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.tinlok.util.toIntHex

// There's like ten RFCs for IPv6!
// RFC 4291 seems to be the most recent authority on text-based encoding of IPv6 addressees.
// RFC 5952 updates 4291 (and previous RFCs) to give better recommendations for encoding them.
// So these classes follow 4291 for the decoding, and then also applies the
// recommendations of 5952 for going from ByteArray -> String.

/**
 * Parses an IPv6 address from a string.
 *
 * .. note::
 *
 *    This treats a right bracket as the final character, and will ignore everything past it.
 */
public class IPv6TextParser(public val incoming: String) {
    // TODO: This doesn't parse subnets yet, which might be fine for our purposes.

    /** An enumeration of all valid states. */
    private enum class State {
        /** Initial (empty) state. */
        STATE_INITIAL,

        /** Last character was a left-bracket '[' */
        STATE_LBRACKET,

        /** Last character was a hex digit. */
        STATE_HEXDIGIT,

        /** Last character was a colon that immediately starts the address */
        STATE_FIRST_COLON,

        /** Last character was a regular colon. */
        STATE_REGULAR_COLON,

        /** Last character was the second colon. */
        STATE_DOUBLE_COLON,

        /** Last character was a right-bracket ']' */
        STATE_RBRACKET,
        ;
    }

    /** The number of digits we've seen in the current token. */
    private var digitCount: Int = 0

    /** The number of colons we've seen up to now. */
    private var colons: Int = 0

    /** If we've already seen a double colon. */
    private var seenDoubleColon: Boolean = false

    /** The current state of the parser. */
    private var state: State = State.STATE_INITIAL

    /** If we started with a left-bracket. */
    private var openBrackets: Boolean = false

    /** The final buffer of bytes. */
    private val buf = ByteArray(16)

    /** Cursor into the final array. */
    private var cursor = 0

    /** Secondary buffer, used for bytes after a double colon. */
    private val secondaryBuf = ByteArray(16)

    /** Secondary cursor, used for the secondary buffer. */
    private var secondaryCursor = 0

    /** Current array of hex digits. */
    private val hexDigits = CharArray(4) { '0' }


    /**
     * Resets all values to their default.
     */
    private fun reset() {
        digitCount = 0
        colons = 0
        seenDoubleColon = false
        state = State.STATE_INITIAL

        buf.fill(0)
        secondaryBuf.fill(0)
        cursor = 0
        secondaryCursor = 0

        hexDigits.fill('0')
    }

    private fun parseError(cause: String): Nothing {
        throw IPv6ParseException(cause, incoming)
    }

    /**
     * Pushes a single digit onto the current digit array.
     */
    private fun pushSingleDigit(digit: Char) {
        // this is always fine because the parser code ensures we don't go above four digits
        hexDigits[0] = hexDigits[1]
        hexDigits[1] = hexDigits[2]
        hexDigits[2] = hexDigits[3]
        hexDigits[3] = digit
    }

    /**
     * Pushes the current hex digit component onto the appropriate array.
     */
    private fun pushComponent() {
        // decode the actual octet into a pair of bytes
        val upperFirst = hexDigits[0].toIntHex().shl(4)
        val lowerFirst = hexDigits[1].toIntHex()
        val first = upperFirst.or(lowerFirst).toByte()
        val upperSecond = hexDigits[2].toIntHex().shl(4)
        val lowerSecond = hexDigits[3].toIntHex()
        val second = upperSecond.or(lowerSecond).toByte()
        // make sure no stale state is around
        hexDigits.fill('0')

        if (seenDoubleColon) {
            // push onto the backwards array, because we don't know where to push anymore!
            secondaryBuf[secondaryCursor++] = first
            secondaryBuf[secondaryCursor++] = second
        } else {
            buf[cursor++] = first
            buf[cursor++] = second
        }

        digitCount = 0
    }

    /**
     * Copies the secondary buffer into the right position.
     */
    private fun copySecondary() {
        val offset = 16 - secondaryCursor
        secondaryBuf.copyInto(buf, destinationOffset = offset, endIndex = secondaryCursor)
    }

    /**
     * Parses the IPv6 address contained within, returning a [ByteArray] of the constituent bytes.
     */
    public fun parse(): ByteArray {
        reset()

        val iterator = incoming.iterator()
        while (true) {
            if (!iterator.hasNext()) break

            when (val nextChar = iterator.nextChar()) {
                '[' -> {
                    // left-brackets can ONLY happen when we're in the initial state, i.e. as the
                    // first character
                    if (state != State.STATE_INITIAL) {
                        parseError("Illegal left bracket")
                    }
                    openBrackets = true
                    state = State.STATE_LBRACKET
                }
                ']' -> {
                    if (!openBrackets) {
                        parseError("Illegal right-bracket when there was no left bracket")
                    }
                    // right bracket can only happen after a digit or double colon
                    if (state != State.STATE_HEXDIGIT && state != State.STATE_DOUBLE_COLON) {
                        parseError("Illegal right bracket")
                    }
                    openBrackets = false
                    state = State.STATE_RBRACKET
                    break
                }
                in '0'..'9', in 'a'..'f', in 'A'..'F' -> {
                    if (state == State.STATE_HEXDIGIT) {
                        // ensure we've only seen up to four hex digits
                        if (digitCount >= 4) {
                            parseError("Too many digits in an octet")
                        }
                    } else if (state == State.STATE_FIRST_COLON) {
                        // a digit after a FIRST COLON means that the address is like
                        // [:1:2] which is obviously illegal.
                        parseError("Illegal leading single colon")
                    }

                    // all non-error states here are valid, and we add on the hex digit
                    pushSingleDigit(nextChar)
                    digitCount += 1
                    state = State.STATE_HEXDIGIT
                }
                ':' -> {
                    // this has to go first, or else we might try and push a bunch of colons
                    // off the side!
                    if (colons >= 8) {
                        parseError("Too many colons!")
                    }

                    if (state == State.STATE_LBRACKET || state == State.STATE_INITIAL) {
                        // left-bracket or initial means we SHOULD be entering the double colon
                        state = State.STATE_FIRST_COLON
                    } else if (state == State.STATE_HEXDIGIT) {
                        // we're a colon after a hex digit, so we push the current component into
                        // the right array
                        pushComponent()
                        state = State.STATE_REGULAR_COLON
                    } else if (
                        state == State.STATE_FIRST_COLON
                        || state == State.STATE_REGULAR_COLON
                    ) {
                        // we're the double colon...
                        // complicates matters significantly
                        if (seenDoubleColon) {
                            parseError("Second double colon detected")
                        }

                        seenDoubleColon = true
                        state = State.STATE_DOUBLE_COLON
                    } else if (state == State.STATE_DOUBLE_COLON) {
                        // 1:::2
                        parseError("Illegal extra colon detected")
                    } else {
                        parseError("Illegal colon in state $state")
                    }

                    if (colons >= 8) {
                        parseError("Too many colons!")
                    }
                    colons += 1
                }
                else -> parseError("Illegal character '$nextChar'")
            }
        }

        // ==  post-condition checks == //

        // if we had a [, we need a ]
        if (openBrackets) {
            parseError("Missing closing bracket")
        }

        // if we finished with a trailing colon, and it's not the double colon, it's an error
        // (e.g. 0: )
        if (state == State.STATE_FIRST_COLON || state == State.STATE_REGULAR_COLON) {
            parseError("Address has trailing colon")
        }

        // all addresses without the :: have seven colons, so we need to check for that
        if (colons < 7 && !seenDoubleColon) {
            parseError("Not enough components in address")
        }

        // make sure we push any trailing digits
        pushComponent()

        // finally, ensure the secondary array (post-::) is copied over.
        copySecondary()
        return buf
    }

    public companion object {
        /**
         * Parses an IPv6 address, returning the parsed octets.
         */
        public fun parse(address: String): ByteArray {
            val parser = IPv6TextParser(address)
            return parser.parse()
        }
    }
}

