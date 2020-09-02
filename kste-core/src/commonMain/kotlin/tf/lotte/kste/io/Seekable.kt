/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste.io

/**
 * Represents an object that is seekable - you can change the cursor position as needed.
 */
public interface Seekable {
    /** The current cursor position. */
    public val cursorPosition: Long

    /**
     * Changes the current cursor position of this Seekable.
     */
    public fun seekAbsolute(position: Long)

    /**
     * Changes the current cursor position of this Seekable.
     */
    public fun seekAbsolute(position: Int): Unit = seekAbsolute(position.toLong())

    /**
     * Changes the current cursor position of this Seekable, relative to the previous position.
     */
    public fun seekRelative(position: Long)

    /**
     * Changes the current cursor position of this Seekable.
     */
    public fun seekRelative(position: Int): Unit = seekRelative(position.toLong())
}
