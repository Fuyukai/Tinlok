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
 * Enumeration of seek "whence" parameters.
 */
public expect enum class SeekWhence {
    /** Seek from the start of the file (absolute seek). */
    START,
    /** Seek from the current position of the file (relative seek). */
    CURRENT,
    /** Seek from the end of the file (absolute seek). */
    END,
    ;

    /** The number of this seek whence value. */
    public val number: Int
}