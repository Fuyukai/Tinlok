/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io.async

/**
 * An object that can be closed, asynchronously.
 */
public interface AsyncCloseable {
    /**
     * Closes this resource.
     *
     * This method is idempotent; subsequent calls will have no effects.
     */
    public suspend fun aclose()
}
