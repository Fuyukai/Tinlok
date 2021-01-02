/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

/**
 * A `Universally Unique Identifier <https://en.wikipedia
 * .org/wiki/Universally_unique_identifier>`__ object.
 *
 * .. warning::
 *
 *     This class only supports generating V1 and V4-based UUIDs, but can parse V3 and V5 UUIDs
 *     as well.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class UUID(
    /** The full bytes of this UUID. */
    public val bytes: ByteString,
) {
    /**
     * An enumeration of valid UUID variants.
     */
    public enum class Variant {
        /** Variant 10X, as specified by RFC 4122. */
        RFC_4122,

        /** Variant 110, for Microsoft GUIDs. */
        MICROSOFT,

        /** Variant 111, reserved for future usage. */
        RESERVED,
        ;
    }

    public enum class Version {
        /** The "NIL" UUID, with all bits set to zero. */
        NIL,

        /** The version one (date-time and MAC address) UUID. */
        VERSION_ONE,

        /** The version two (DCE security) UUIDD. */
        VERSION_TWO,

        /** The version three (MD5 namespace) UUID. */
        VERSION_THREE,

        /** The version four (psuedorandom) UUID. */
        VERSION_FOUR,

        /** The version five (SHA-1 namespace) UUID. */
        VERSION_FIVE,
        ;
    }

    init {
        require(bytes.size == 16) { "UUID must be 16 bytes long" }
    }

    // public fields
    /** The low field of the timestamp. */
    public val timeLow: UInt
        get() {
            return when (variant) {
                Variant.RFC_4122 -> {
                    // big endian
                    bytes.toInt(offset = 0).toUInt()
                }
                Variant.MICROSOFT -> {
                    // old MS uuids are little endian
                    bytes.toIntLE(offset = 0).toUInt()
                }
                else -> throw UnsupportedOperationException("Unknown variant: $variant")
            }
        }

    /** The middle field of the timestamp. */
    public val timeMid: UShort
        get() {
            return when (variant) {
                Variant.RFC_4122 -> bytes.toShort(offset = 4).toUShort()
                Variant.MICROSOFT -> bytes.toShortLE(offset = 4).toUShort()
                else -> throw UnsupportedOperationException("Unknown variant: $variant")
            }
        }

    /** The high field of the timestamp, multiplexed with the version. */
    public val timeHighAndVersion: UShort
        get() {
            return when (variant) {
                Variant.RFC_4122 -> bytes.toShort(offset = 6).toUShort()
                Variant.MICROSOFT -> bytes.toShortLE(offset = 6).toUShort()
                else -> throw UnsupportedOperationException("Unknown variant: $variant")
            }
        }

    /** The high field of the clock sequence, multiplexed with the variant. */
    public val clockSeqHighAndVariant: UByte
        get() = bytes[8].toUByte()

    /** The low field of the clock sequence. */
    public val clockSeqLow: UByte
        get() = bytes[9].toUByte()

    /** The node identifier. */
    public val node: ByteString
        get() = bytes.substring(start = 10, end = 16)

    // depends on clockSeqHighAndVariant
    /** The variant of this UUID. */
    public val variant: Variant
        get() {
            // RFC 4122 defines this in the worst way possible!
            // Bit 0 (Msb0) should always be 1.
            if (!clockSeqHighAndVariant.bit(8)) {
                throw UnsupportedOperationException("Legacy NCS UUIDs are not supported")
            }

            // MSB1
            return when (clockSeqHighAndVariant.bit(7)) {
                false -> Variant.RFC_4122
                true -> Variant.MICROSOFT
            }
        }

    /** The version of this UUID. */
    public val version: Version
        get() {
            val value = (bytes[6]).toInt().ushr(4)
            return Version.values()[value]
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UUID) return false

        return other.bytes == bytes
    }

    override fun hashCode(): Int {
        return bytes.hashCode()
    }

    override fun toString(): String {
        // inefficient method!
        val hexString = bytes.hexlify().toCharArray()
        val chars = CharArray(36)
        // copy each field into the new arr and separate with strings

        // time_low
        hexString.copyInto(chars, destinationOffset = 0, startIndex = 0, endIndex = 8)
        chars[8] = '-'
        // time_mid
        hexString.copyInto(chars, destinationOffset = 9, startIndex = 8, endIndex = 12)
        chars[13] = '-'
        // time_hi_and_version
        hexString.copyInto(chars, destinationOffset = 14, startIndex = 12, endIndex = 16)
        chars[18] = '-'
        // clock_seq_hi_and_variant + clock_seq_low
        hexString.copyInto(chars, destinationOffset = 19, startIndex = 16, endIndex = 20)
        chars[23] = '-'
        // node
        hexString.copyInto(chars, destinationOffset = 24, startIndex = 20, endIndex = 32)

        return chars.concatToString()
    }

    public companion object {
        /** The nil UUID (all zeroes). */
        public val NIL: UUID = UUID(ByteString.zeroed(16))

        /**
         * Creates a new fully psuedo-random UUID.
         */
        public fun uuid4(): UUID {
            val bytes = uuidGenerateV4()
            return UUID(bytes.toByteString())
        }

        /**
         * Creates a new UUID that uses this computer's MAC address to ensure uniqueness.
         */
        public fun uuid1(): UUID {
            val bytes = uuidGenerateV1()
            return UUID(bytes.toByteString())
        }

        /**
         * Parses a UUID from a string.
         */
        public fun fromString(uuid: String): UUID {
            // this is the reverse of toString, kind of
            val chars = CharArray(32)
            val uuidChars = uuid.toCharArray()
            uuidChars.copyInto(chars, 0, 0, 8)
            uuidChars.copyInto(chars, 8, 9, 13)
            uuidChars.copyInto(chars, 12, 14, 18)
            uuidChars.copyInto(chars, 16, 19, 23)
            uuidChars.copyInto(chars, 20, 24, 36)
            val hexString = chars.concatToString()
            val bs = hexString.unhexlify()
            return UUID(bs)
        }
    }

}
