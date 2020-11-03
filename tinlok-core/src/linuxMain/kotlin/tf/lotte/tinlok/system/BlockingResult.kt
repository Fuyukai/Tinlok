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
public inline class BlockingResult(public val count: Int) {
    /**
     * If this result didn't need a blocking call to be complete.
     */
    public val isSuccess: Boolean
        get() = count != -1
}
