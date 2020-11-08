/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io.async

/**
 * Marks an object that can be selected on.
 */
public interface Selectable {
    /**
     * Gets the selection key for this selectable.
     */
    public fun key(): SelectionKey
}
