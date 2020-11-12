/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import kotlinx.cinterop.*
import tf.lotte.tinlok.util.*
import kotlin.experimental.and

/**
 * A [Buffer] that wraps a [ByteArray].
 */
public actual class ByteArrayBuffer
actual constructor(private val ba: ByteArray): Buffer {
    public actual companion object;

    // the real cursor, to avoid extra long conversions
    private var realCursor: Int = 0

    /**
     * The current cursor for this buffer. Cannot be negative, nor can it overflow the capacity.
     */
    actual override var cursor: Long
        get() = realCursor.toLong()
        set(value) {
            require(value <= Int.MAX_VALUE) { "Cursor larger than INT_MAX" }
            realCursor = value.toInt()
        }

    /**
     * The maximum position for this buffer. Cannot be negative, and cannot be (directly) changed.
     */
    actual override val capacity: Long get() = ba.size.toLong()

    /**
     * Checks if we have the [needed] capacity available for writing.
     */
    private fun checkCapacity(needed: Int, read: Boolean) {
        if (cursor + needed > capacity) {
            if (read) TODO("Buffer underflow")
            else TODO("Buffer overflow")
        }
    }

    /**
     * Reads a single byte off of the buffer at the current cursor, advancing it by one.
     */
    actual override fun readByte(): Byte {
        checkCapacity(1, read = true)
        return ba[realCursor++]
    }

    /**
     * Reads a single short off of the buffer at the current cursor, advancing it by two.
     */
    actual override fun readShort(): Short {
        checkCapacity(2, read = true)

        val s = ba.toShort(realCursor)
        realCursor += 2
        return s
    }

    /**
     * Reads a single short (in little endian) off of the buffer at the current cursor, advancing
     * it by two.
     */
    actual override fun readShortLE(): Short {
        checkCapacity(2, read = true)

        val s = ba.toShortLE(realCursor)
        realCursor += 2
        return s
    }

    /**
     * Reads a single int off of the buffer at the current cursor, advancing it by four.
     */
    actual override fun readInt(): Int {
        checkCapacity(4, read = true)

        val i = ba.toInt(realCursor)
        realCursor += 4
        return i
    }

    /**
     * Reads a single int (in little endian) off of the buffer at the current cursor, advancing it
     * by four.
     */
    actual override fun readIntLE(): Int {
        checkCapacity(4, read = true)

        val i = ba.toIntLE(realCursor)
        realCursor += 4
        return i
    }

    /**
     * Reads a single long off of the buffer at the current cursor, advancing it by eight.
     */
    actual override fun readLong(): Long {
        checkCapacity(8, read = true)

        val l = ba.toLong(realCursor)
        realCursor += 4
        return l
    }

    /**
     * Reads a single long (in little endian) off of the buffer at the current cursor, advancing it
     * by eight.
     */
    actual override fun readLongLE(): Long {
        checkCapacity(8, read = true)

        val l = ba.toLongLE(realCursor)
        realCursor += 4
        return l
    }

    /**
     * Writes a single byte to this buffer at the current cursor, advancing it by one.
     */
    actual override fun writeByte(b: Byte) {
        checkCapacity(1, read = false)

        ba[realCursor++] = b
    }

    /**
     * Writes a single short to this buffer at the current cursor, advancing it by two.
     */
    actual override fun writeShort(short: Short) {
        checkCapacity(2, read = false)

        val upper = short.toInt().and(0xff00).ushr(4).toByte()
        val lower = short.and(0xff).toByte()
        ba[realCursor++] = upper
        ba[realCursor++] = lower
    }

    /**
     * Writes a single short to this buffer in little endian, advancing it by two.
     */
    actual override fun writeShortLE(short: Short) {
        checkCapacity(2, read = false)

        val upper = short.toInt().and(0xff00).ushr(4).toByte()
        val lower = short.and(0xff).toByte()
        ba[realCursor++] = lower
        ba[realCursor++] = upper
    }

    /**
     * Writes a single int to this buffer at the current cursor, advancing it by four.
     */
    actual override fun writeInt(int: Int) {
        checkCapacity(4, read = false)

        val bytes = int.toByteArray()
        bytes.copyInto(ba, realCursor)
        realCursor += 4
    }

    /**
     * Writes a single int to this buffer in little endian, advancing it by four.
     */
    actual override fun writeIntLE(int: Int) {
        checkCapacity(4, read = false)

        val bytes = int.toByteArrayLE()
        bytes.copyInto(ba, realCursor)
        realCursor += 4
    }

    /**
     * Writes a single long to this buffer at the current cursor, advancing it by eight.
     */
    actual override fun writeLong(long: Long) {
        checkCapacity(8, read = false)

        val bytes = long.toByteArray()
        bytes.copyInto(ba, realCursor)
        realCursor += 8
    }

    /**
     * Writes a single long to this buffer in little endian, advancing it by eight.
     */
    actual override fun writeLongLE(long: Long) {
        checkCapacity(8, read = false)

        val bytes = long.toByteArrayLE()
        bytes.copyInto(ba, realCursor)
        realCursor += 8
    }

    /**
     * Writes [size] bytes from [array] into this buffer, starting at [offset] in the incoming
     * array.
     */
    actual override fun writeFrom(array: ByteArray, size: Int, offset: Int) {
        require(offset + size <= array.size) {
            "offset $offset + size $size > array size ${array.size}"
        }

        checkCapacity(size, read = false)

        array.copyInto(array, realCursor, offset, offset + size)
    }

    /**
     * Returns true if this supports reading/writing directly to a memory address,
     * or if it needs to go through an intermediate [ByteArray] first.
     */
    override fun supportsAddress(): Boolean = true

    /**
     * Pins the backing object for this [Buffer], then gets the memory address of the first item.
     */
    override fun <R> address(offset: Long, block: (CPointer<ByteVar>) -> R): R {
        return ba.usePinned {
            block(it.addressOf(offset.toInt()))
        }
    }

    // Close does nothing for this type
    /**
     * Closes this resource.
     *
     * This method is idempotent; subsequent calls will have no effects.
     */
    override fun close() {}

}
