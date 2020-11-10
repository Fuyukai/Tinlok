/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

/**
 * "Union" type for would block vs would not block.
 *
 * If [count] is -1, the result of the platform call was EAGAIN/EWOULDBLOCK/EINPROGRESS. Otherwise, it
 * is usually a number that makes sense in context of the platform call.
 */
public inline class BlockingResult(public val count: Long) {
    public companion object {
        /** Singleton failure result. */
        public val WOULD_BLOCK: BlockingResult = BlockingResult(-1L)

        /** Singleton success result, for results without a meaningful count. */
        public val DIDNT_BLOCK: BlockingResult = BlockingResult(0L)
    }

    /**
     * If this result didn't need a blocking call to be complete.
     */
    public inline val isSuccess: Boolean
        get() = count != -1L
}

public inline fun BlockingResult.ensureNonBlock(): Long {
    // todo: change error?
    if (!isSuccess) throw IllegalStateException(
        "Underlying operation is non-blocking, but was called in a blocking context"
    )
    return count
}
