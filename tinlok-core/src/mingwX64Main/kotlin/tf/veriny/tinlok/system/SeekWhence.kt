/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.system

import platform.windows.FILE_BEGIN
import platform.windows.FILE_CURRENT
import platform.windows.FILE_END

/**
 * Enumeration of seek "whence" parameters.
 */
public actual enum class SeekWhence(public actual val number: Int) {
    /** Seek from the start of the file (absolute seek). */
    START(FILE_BEGIN),

    /** Seek from the current position of the file (relative seek). */
    CURRENT(FILE_CURRENT),

    /** Seek from the end of the file (absolute seek). */
    END(FILE_END),
    ;
}
