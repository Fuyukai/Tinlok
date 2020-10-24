/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.types

/**
 * Represents a backing implementation for a [ByteString].
 */
public interface ByteStringHolder : Iterable<Byte> {
    /** The size of this holder. */
    public val size: Int

    /**
     * Decodes this ByteStringHolder into a regular [String].
     */
    public fun decode(): String

    /**
     * Gets a single byte from this holder at the specified index.
     */
    public operator fun get(idx: Int): Byte

    /**
     * Checks if this holder contains the specified other byte.
     */
    public operator fun contains(other: Byte): Boolean

    /**
     * Converts this holder to a mutable [ByteArray]. This may copy it.
     */
    public fun unwrap(): ByteArray

    /**
     * Concatenates this holder with a different ByteArray, returning a new holder.
     */
    public fun concatenate(other: ByteArray): ByteStringHolder
}
