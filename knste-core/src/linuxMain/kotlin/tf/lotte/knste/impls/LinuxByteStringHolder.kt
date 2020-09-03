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
 * Linux implementation of the ByteString holder.
 */
internal class LinuxByteStringHolder(ba: ByteArray) : Iterable<Byte>  {
    internal companion object {
        internal fun stringToByteArray(s: String): ByteStringHolder {
            return LinuxByteStringHolder(s.encodeToByteArray())
        }

        /**
         * Creates a holder from a ByteArray with copying.
         */
        internal fun fromByteArray(ba: ByteArray): ByteStringHolder {
            return LinuxByteStringHolder(ba.copyOf())
        }

        /**
         * Creates a holder from a ByteArray without copying.
         */
        internal fun fromByteArrayUncopied(ba: ByteArray): ByteStringHolder {
            return LinuxByteStringHolder(ba)
        }
    }

    // always take a copy
    private val ba: ByteArray = ba.copyOf()

    inner class __Iterator : Iterator<Byte> by ba.iterator()

    val size: Int
        get() = ba.size

    operator fun get(index: Int): Byte {
        return ba[index]
    }

    fun decode(): String {
        return ba.decodeToString()
    }

    fun contains(byte: Byte): Boolean {
        return ba.contains(byte)
    }

    override fun iterator(): Iterator<Byte> = __Iterator()
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ByteStringHolder) return false
        return other.ba.contentEquals(ba)
    }
    override fun hashCode(): Int {
        return ba.contentHashCode()
    }
}

internal actual typealias ByteStringHolder = LinuxByteStringHolder
