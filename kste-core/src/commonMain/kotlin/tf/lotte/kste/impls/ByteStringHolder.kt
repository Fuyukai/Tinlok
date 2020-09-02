/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */


package tf.lotte.kste.impls

/**
 * Platform implementation object for ByteStrings.
 */
internal expect class ByteStringHolder(ba: ByteArray) : Iterable<Byte> {
    internal companion object {
        /**
         * Converts a [String] to a [ByteStringHolder] using UTF-8.
         */
        internal fun stringToByteArray(s: String): ByteStringHolder
    }
    /** The size of this ByteString. */
    val size: Int

    /** Gets a single byte. */
    operator fun get(index: Int): Byte

    /** Decodes this holder using UTF-8. */
    fun decode(): String

    /**
     * Checks if this holder contains the specified byte.
     */
    fun contains(byte: Byte): Boolean
}
