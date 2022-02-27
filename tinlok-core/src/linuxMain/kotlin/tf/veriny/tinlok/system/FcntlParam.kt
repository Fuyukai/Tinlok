/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.system

/**
 * An class wrapping valid (known) FCNTL parameters.
 */
public class FcntlParam<T>(public val number: Int) {
    public companion object {
        /** Gets the flags for a file descriptor. */
        public val F_GETFD: FcntlParam<Int> = FcntlParam(platform.posix.F_GETFD)

        /** Sets the flags for a file descriptor. */
        public val F_SETFD: FcntlParam<Int> = FcntlParam(platform.posix.F_SETFD)

        /** Gets the file status flags from an FD. */
        public val F_GETFL: FcntlParam<Int> = FcntlParam(platform.posix.F_GETFL)

        /** Sets the file status flags for an FD. */
        public val F_SETFL: FcntlParam<Int> = FcntlParam(platform.posix.F_SETFL)
    }
}
