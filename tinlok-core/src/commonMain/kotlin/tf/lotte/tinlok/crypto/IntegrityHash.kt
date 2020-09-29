/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.types.bytestring.ByteString
import tf.lotte.tinlok.types.bytestring.hexlify

/**
 * Represents an integrity hash (NOT a password hash).
 */
@OptIn(ExperimentalUnsignedTypes::class)
public inline class IntegrityHash(public val bytes: ByteString) {
    /**
     * Securely verifies this integrity hash against another hash.
     *
     * You should always use this to compare two hashes together, as this will run in constant time.
     */
    public fun verify(other: ByteString): Boolean {
        val first = bytes.unwrap().toUByteArray()
        val second = other.unwrap().toUByteArray()
        return CryptographyProvider.current().secureCompare(first, second)
    }

    /** Shortcut for ``verify(other.bytes)``. */
    public fun verify(other: IntegrityHash): Boolean = verify(other.bytes)

    /** Creates a new hex digest of this integrity hash. */
    public fun hexdigest(): String = bytes.hexlify()
}
