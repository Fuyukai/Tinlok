/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste

import tf.lotte.knste.types.bytestring.ByteArrayByteStringHolder
import tf.lotte.knste.types.bytestring.ByteStringHolder


/**
 * Represents an immutable string of singular bytes.
 */
public class ByteString
private constructor(
    private val backing: ByteStringHolder
) : Iterable<Byte>, Collection<Byte> {
    public companion object {
        /**
         * Creates a new [ByteString] from a [ByteArray].
         */
        public fun fromByteArray(ba: ByteArray): ByteString =
            ByteString(ByteArrayByteStringHolder(ba.copyOf()))

        /**
         * Creates a new [ByteString] from a [String].
         */
        public fun fromString(string: String): ByteString {
            return ByteString(ByteArrayByteStringHolder(string.encodeToByteArray()))
        }

        /**
         * Creates a new [ByteString] from a [ByteArray], without copying it.
         */
        internal fun fromUncopied(ba: ByteArray): ByteString =
            ByteString(ByteArrayByteStringHolder(ba))

        /**
         * Creates
         */
        internal fun fromRawHolder(holder: ByteStringHolder): ByteString = ByteString(holder)
    }

    /** The size of this ByteString. */
    public override val size: Int
        get() = backing.size

    /**
     * Decodes this [ByteString] to a UTF-8 [String].
     */
    public fun decode(): String {
        return backing.decode()
    }

    /**
     * Gets a single byte from this ByteString.
     */
    public operator fun get(idx: Int): Byte {
        return backing[idx]
    }

    /**
     * Concatenates two [ByteString] instances, returning a new [ByteString].
     */
    public operator fun plus(other: ByteString): ByteString {
        return ByteString(backing.concatenate(other.unwrap()))
    }

    /**
     * Checks if this [ByteString] contains a specific byte [element].
     */
    public override operator fun contains(element: Byte): Boolean {
        return backing.contains(element)
    }

    /**
     * Checks if this ByteString contains all of the bytes in [elements].
     */
    public override fun containsAll(elements: Collection<Byte>): Boolean {
        return elements.all { this.contains(it) }
    }

    /**
     * Checks if this ByteString is empty.
     */
    public override fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * Checks if this ByteString starts with a the byte [other].
     */
    public fun startsWith(other: Byte): Boolean {
        return get(0) == other
    }

    /**
     * Checks if this ByteString starts with a different ByteString.
     */
    public fun startsWith(other: ByteString): Boolean {
        if (other.size > size) return false

        for (idx in other.indices) {
            val ours = get(idx)
            val theirs = other[idx]
            if (ours != theirs) return false
        }

        return true
    }

    /**
     * Unwraps this [ByteString], getting the underlying array.
     */
    internal fun unwrap(): ByteArray {
        return backing.unwrap()
    }

    override fun iterator(): Iterator<Byte> {
        return backing.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ByteString) return false
        return backing == other.backing
    }

    override fun hashCode(): Int {
        return backing.hashCode()
    }

    override fun toString(): String {
        val s = this.joinToString("") {
            if (it in 32..126) it.toChar().toString()
            else "\\x" + it.toUByte().toString(16).padStart(2, '0')
        }
        return "b(\"$s\")"
    }
}
