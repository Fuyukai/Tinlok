/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */


package tf.lotte.knste.impls

/**
 * Platform implementation object for ByteStrings.
 */
internal expect class ByteStringHolder private constructor(ba: ByteArray) : Iterable<Byte> {
    internal companion object {
        /**
         * Converts a [String] to a [ByteStringHolder] using UTF-8.
         */
        internal fun stringToByteArray(s: String): ByteStringHolder

        /**
         * Creates a holder from a ByteArray with copying.
         */
        internal fun fromByteArray(ba: ByteArray): ByteStringHolder

        /**
         * Creates a holder from a ByteArray without copying.
         */
        internal fun fromByteArrayUncopied(ba: ByteArray): ByteStringHolder
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

    /**
     * Gets the backing [ByteArray] for this holder, or a new [ByteArray] with the contents of
     * this holder.
     */
    fun unwrap(): ByteArray
}
