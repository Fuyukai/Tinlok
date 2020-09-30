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
import tf.lotte.tinlok.types.bytestring.toByteString

// == Blake2b shortcuts == //
/** Creates a new [IntegrityHash] for this [ByteString]. */
public fun ByteString.integrityHash(algorithm: String? = null): IntegrityHash {
    return IntegrityHasher.blake2b {
        it.feed(this)
        it.hash()
    }
}

/** Creates a new [IntegrityHash] for this [ByteArray]. */
public fun ByteArray.integrityHash(algorithm: String? = null): IntegrityHash =
    toByteString().integrityHash(algorithm)

/** Creates a new [IntegrityHash] for this [String]. */
public fun String.integrityHash(algorithm: String? = null): IntegrityHash =
    toByteString().integrityHash(algorithm)

