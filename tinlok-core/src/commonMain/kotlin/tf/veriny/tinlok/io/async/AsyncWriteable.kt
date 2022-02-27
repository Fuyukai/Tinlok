/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io.async

import tf.veriny.tinlok.io.Writeable

/**
 * An asynchronous version of [Writeable].
 */
public interface AsyncWriteable {
    /**
     * Attempts to write the entirety of the buffer [buf] to this object, returning the number of
     * bytes actually written before reaching EOF.
     *
     * This will suspend until *all* data is written, or until EOF is reached.
     */
    public suspend fun writeAllFrom(buf: ByteArray): Int
}
