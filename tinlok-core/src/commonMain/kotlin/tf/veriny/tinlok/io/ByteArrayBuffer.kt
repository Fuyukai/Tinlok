/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io

/**
 * A [Buffer] that wraps a [ByteArray].
 */
public expect class ByteArrayBuffer
constructor(ba: ByteArray) : Buffer {
    public companion object;

    // ALl copied from Buffer to work around really stupid expect/actual bug
    /**
     * The current cursor for this buffer. Cannot be negative, nor can it overflow the capacity.
     */
    public override var cursor: Long

    /**
     * The maximum position for this buffer. Cannot be negative, and cannot be (directly) changed.
     */
    public override val capacity: Long

    // == Read methods == //

    /**
     * Reads a single byte off of the buffer at the current cursor, advancing it by one.
     */
    public override fun readByte(): Byte

    /**
     * Reads a single short off of the buffer at the current cursor, advancing it by two.
     */
    public override fun readShort(): Short

    /**
     * Reads a single short (in little endian) off of the buffer at the current cursor, advancing
     * it by two.
     */
    public override fun readShortLE(): Short

    /**
     * Reads a single int off of the buffer at the current cursor, advancing it by four.
     */
    public override fun readInt(): Int

    /**
     * Reads a single int (in little endian) off of the buffer at the current cursor, advancing it
     * by four.
     */
    public override fun readIntLE(): Int

    /**
     * Reads a single long off of the buffer at the current cursor, advancing it by eight.
     */
    public override fun readLong(): Long

    /**
     * Reads a single long (in little endian) off of the buffer at the current cursor, advancing it
     * by eight.
     */
    public override fun readLongLE(): Long

    /**
     * Reads a [ByteArray] of size [count] off this buffer.
     */
    public override fun readArray(count: Int): ByteArray

    // == Write methods == //
    /**
     * Writes a single byte to this buffer at the current cursor, advancing it by one.
     */
    public override fun writeByte(b: Byte)

    /**
     * Writes a single short to this buffer at the current cursor, advancing it by two.
     */
    public override fun writeShort(short: Short)

    /**
     * Writes a single short to this buffer in little endian, advancing it by two.
     */
    public override fun writeShortLE(short: Short)

    /**
     * Writes a single int to this buffer at the current cursor, advancing it by four.
     */
    public override fun writeInt(int: Int)

    /**
     * Writes a single int to this buffer in little endian, advancing it by four.
     */
    public override fun writeIntLE(int: Int)

    /**
     * Writes a single long to this buffer at the current cursor, advancing it by eight.
     */
    public override fun writeLong(long: Long)

    /**
     * Writes a single long to this buffer in little endian, advancing it by eight.
     */
    public override fun writeLongLE(long: Long)

    /**
     * Writes [size] bytes from [array] into this buffer, starting at [offset] in the incoming
     * array.
     */
    public override fun writeFrom(array: ByteArray, size: Int, offset: Int)
}

/**
 * Creates a new [ByteArrayBuffer] of the specified [size], initialised to zero.
 */
public fun ByteArrayBuffer.Companion.ofSize(size: Int): ByteArrayBuffer {
    val ba = ByteArray(size) { 0 }
    return ByteArrayBuffer(ba)
}
