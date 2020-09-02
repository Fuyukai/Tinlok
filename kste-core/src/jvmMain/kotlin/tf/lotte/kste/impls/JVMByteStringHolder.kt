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
 * The JVM implementation of [ByteStringHolder].
 */
internal class JVMByteStringHolder(ba: ByteArray) : Iterable<Byte> {
    internal companion object {
        internal fun stringToByteArray(s: String): ByteStringHolder {
            return ByteStringHolder(s.toByteArray(Charsets.UTF_8))
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
        return ba.toString(Charsets.UTF_8)
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

internal actual typealias ByteStringHolder = JVMByteStringHolder
