/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc

/**
 * Represents any object that is closeable.
 */
public expect interface Closeable {
    /**
     * Closes this resource.
     *
     * This method is idempotent; subsequent calls will have no effects.
     */
    public fun close()
}

/**
 * Using the specified [Closeable], runs the lambda [block] and automatically closes the object
 * afterwards.
 */
public expect inline fun <T : Closeable, R> T.use(block: (T) -> R): R
