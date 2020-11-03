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
 * An enumeration of valid (known) FCNTL parameters.
 */
public enum class FcntlParam(public val number: Int) {
    /** Gets the flags for a file descriptor. */
    F_GETFD(platform.posix.F_GETFD),
    /** Sets the flags for a file descriptor */
    F_SETFD(platform.posix.F_SETFD),

    /** Gets the file status flags from an FD. */
    F_GETFL(platform.posix.F_GETFL),
    /** Sets the filr status flags for an FD. */
    F_SETFL(platform.posix.F_SETFL),
    ;
}
