/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.Unsafe
import kotlin.experimental.ExperimentalTypeInference

@Unsafe
internal inline fun TlsContext.ctxerr(name: String, block: () -> Int) {
    val res = block()
    if (res == 0) {
        close()
        throw IllegalStateException("context call failed at $name")
    }
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@Unsafe
internal inline fun TlsContext.ctxerr(name: String, block: () -> Long) {
    val res = block()
    if (res == 0L) {
        close()
        throw IllegalStateException("context call failed at $name")
    }
}

@Unsafe
internal inline fun TlsObject.sslerr(name: String, block: () -> Int) {
    val res = block()
    if (res == 0) {
        close()
        throw IllegalStateException("ssl call failed at $name")
    }
}


@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@Unsafe
internal inline fun TlsObject.sslerr(name: String, block: () -> Long) {
    val res = block()
    if (res == 0L) {
        close()
        throw IllegalStateException("ssl call failed at $name")
    }
}
