/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

import tf.lotte.tinlok.ByteString
import tf.lotte.tinlok.io.use
import tf.lotte.tinlok.toByteString
import tf.lotte.tinlok.util.Unsafe

/**
 * High-level helper for getting an integrity hasher and safely closing it. The hasher will be
 * passed to the specified [block]; it is expected you return your hash from this lambda.
 *
 * If [algorithm] is null, the default algorithm will be used.
 */
@OptIn(Unsafe::class)
public inline fun <R> IntegrityHasher.Companion.hash(
    algorithm: String? = null, block: (IntegrityHasher) -> R
): R {
    val hasher = unsafeGetInstance(algorithm)
    return hasher.use(block)
}

/** Creates a new [IntegrityHash] for this [ByteString]. */
public fun ByteString.integrityHash(algorithm: String? = null): IntegrityHash {
    return IntegrityHasher.hash(algorithm) {
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

