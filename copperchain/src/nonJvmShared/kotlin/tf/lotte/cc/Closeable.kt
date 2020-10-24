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
public actual interface Closeable {
    /**
     * Closes this resource.
     *
     * This method is idempotent; subsequent calls will have no effects.
     */
    public actual fun close()
}
