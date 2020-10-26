/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import platform.posix.SEEK_CUR
import platform.posix.SEEK_END
import platform.posix.SEEK_SET

/**
 * Enumeration of seek "whence" parameters.
 */
public actual enum class SeekWhence(public actual val number: Int) {
    /** Seek from the start of the file (absolute seek). */
    START(SEEK_SET),
    /** Seek from the current position of the file (relative seek). */
    CURRENT(SEEK_CUR),
    /** Seek from the end of the file (absolute seek). */
    END(SEEK_END),
    ;
}