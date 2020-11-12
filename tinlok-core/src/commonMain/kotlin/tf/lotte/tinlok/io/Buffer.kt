/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.util.Closeable

/**
 * A read-write block of binary data in memory.
 *
 * A buffer has two special values: the ``cursor`` and the ``capacity``. The cursor defines the
 * position from which things will be read from, and the capacity defines the maximum position.
 * The capacity value *may* change, if (for example) the buffer implementation automatically resizes
 * with writes.
 *
 * Buffers may be backed by managed Kotlin heap memory, un-managed heap memory, a file, or any
 * other similar backing storage. Make no assumptions about the underlying storage.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect interface Buffer : Closeable {
    // TODO: This will become a ULong when unsigned types suck less.
    /**
     * The current cursor for this buffer. Cannot be negative, nor can it overflow the capacity.
     */
    public var cursor: Long

    /**
     * The maximum position for this buffer. Cannot be negative, and cannot be (directly) changed.
     */
    public val capacity: Long

    // == Read methods == //

    /**
     * Reads a single byte off of the buffer at the current cursor, advancing it by one.
     */
    public fun readByte(): Byte

    /**
     * Reads a single short off of the buffer at the current cursor, advancing it by two.
     */
    public fun readShort(): Short

    /**
     * Reads a single short (in little endian) off of the buffer at the current cursor, advancing
     * it by two.
     */
    public fun readShortLE(): Short

    /**
     * Reads a single int off of the buffer at the current cursor, advancing it by four.
     */
    public fun readInt(): Int

    /**
     * Reads a single int (in little endian) off of the buffer at the current cursor, advancing it
     * by four.
     */
    public fun readIntLE(): Int

    /**
     * Reads a single long off of the buffer at the current cursor, advancing it by eight.
     */
    public fun readLong(): Long

    /**
     * Reads a single long (in little endian) off of the buffer at the current cursor, advancing it
     * by eight.
     */
    public fun readLongLE(): Long

    // == Write methods == //
    /**
     * Writes a single byte to this buffer at the current cursor, advancing it by one.
     */
    public fun writeByte(b: Byte)

    /**
     * Writes a single short to this buffer at the current cursor, advancing it by two.
     */
    public fun writeShort(short: Short)

    /**
     * Writes a single short to this buffer in little endian, advancing it by two.
     */
    public fun writeShortLE(short: Short)

    /**
     * Writes a single int to this buffer at the current cursor, advancing it by four.
     */
    public fun writeInt(int: Int)

    /**
     * Writes a single int to this buffer in little endian, advancing it by four.
     */
    public fun writeIntLE(int: Int)

    /**
     * Writes a single long to this buffer at the current cursor, advancing it by eight.
     */
    public fun writeLong(long: Long)

    /**
     * Writes a single long to this buffer in little endian, advancing it by eight.
     */
    public fun writeLongLE(long: Long)

    /**
     * Writes [size] bytes from [array] into this buffer, starting at [offset] in the incoming
     * array.
     */
    public fun writeFrom(array: ByteArray, size: Int, offset: Int)

}
