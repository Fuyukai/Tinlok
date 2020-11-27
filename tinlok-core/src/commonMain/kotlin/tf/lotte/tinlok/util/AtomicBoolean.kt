/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

/**
 * An atomic boolean that works safely across threads.
 */
public expect class AtomicBoolean(initial: Boolean) {
    /**
     * The actual value of this boolean.
     */
    public var value: Boolean

    /**
     * Gets the inversion of this atomic boolean.
     */
    public operator fun not(): Boolean

    /**
     * Compares the value within this boolean to [expected], and then sets it to [new].
     */
    public fun compareAndSet(expected: Boolean, new: Boolean): Boolean
}
