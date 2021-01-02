/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import tf.lotte.tinlok.Unsafe
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
public actual interface Buffer : Closeable {
    // TODO: This will become a ULong when unsigned types suck less.
    /**
     * The current cursor for this buffer. Cannot be negative, nor can it overflow the capacity.
     */
    public actual var cursor: Long

    /**
     * The maximum position for this buffer. Cannot be negative, and cannot be (directly) changed.
     */
    public actual val capacity: Long

    // == Read methods == //

    /**
     * Reads a single byte off of the buffer at the current cursor, advancing it by one.
     */
    public actual fun readByte(): Byte

    /**
     * Reads a single short off of the buffer at the current cursor, advancing it by two.
     */
    public actual fun readShort(): Short

    /**
     * Reads a single short (in little endian) off of the buffer at the current cursor, advancing
     * it by two.
     */
    public actual fun readShortLE(): Short

    /**
     * Reads a single int off of the buffer at the current cursor, advancing it by four.
     */
    public actual fun readInt(): Int

    /**
     * Reads a single int (in little endian) off of the buffer at the current cursor, advancing it
     * by four.
     */
    public actual fun readIntLE(): Int

    /**
     * Reads a single long off of the buffer at the current cursor, advancing it by eight.
     */
    public actual fun readLong(): Long

    /**
     * Reads a single long (in little endian) off of the buffer at the current cursor, advancing it
     * by eight.
     */
    public actual fun readLongLE(): Long

    /**
     * Reads a [ByteArray] of size [count] off this buffer.
     */
    public actual fun readArray(count: Int): ByteArray

    // == Write methods == //
    /**
     * Writes a single byte to this buffer at the current cursor, advancing it by one.
     */
    public actual fun writeByte(b: Byte)

    /**
     * Writes a single short to this buffer at the current cursor, advancing it by two.
     */
    public actual fun writeShort(short: Short)

    /**
     * Writes a single short to this buffer in little endian, advancing it by two.
     */
    public actual fun writeShortLE(short: Short)

    /**
     * Writes a single int to this buffer at the current cursor, advancing it by four.
     */
    public actual fun writeInt(int: Int)

    /**
     * Writes a single int to this buffer in little endian, advancing it by four.
     */
    public actual fun writeIntLE(int: Int)

    /**
     * Writes a single long to this buffer at the current cursor, advancing it by eight.
     */
    public actual fun writeLong(long: Long)

    /**
     * Writes a single long to this buffer in little endian, advancing it by eight.
     */
    public actual fun writeLongLE(long: Long)

    /**
     * Writes [size] bytes from [array] into this buffer, starting at [offset] in the incoming
     * array.
     */
    public actual fun writeFrom(array: ByteArray, size: Int, offset: Int)

    /**
     * Returns true if this supports reading/writing directly to a memory address,
     * or if it needs to go through an intermediate [ByteArray] first.
     */
    public fun supportsAddress(): Boolean

    /**
     * Pins the backing object for this [Buffer], then gets the memory address of the item at
     * [offset]. This is relative to the cursor.
     *
     * This will raise if [supportsAddress] returns false.
     */
    @Unsafe
    @Throws(UnsupportedOperationException::class)
    public fun <R> address(offset: Long, block: (CPointer<ByteVar>) -> R): R
}

