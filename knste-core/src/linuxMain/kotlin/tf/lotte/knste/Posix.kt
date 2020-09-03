/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste

import kotlinx.cinterop.*
import platform.posix._SC_GETPW_R_SIZE_MAX
import platform.posix.getpwuid_r
import platform.posix.passwd
import platform.posix.sysconf

/**
 * Gets an /etc/passwd entry, or null if it doesn't exist.
 */
internal fun MemScope.getPasswdEntry(uid: UInt): passwd? {
    val passwd = alloc<passwd>()
    val starResult = allocPointerTo<passwd>()

    var bufSize = sysconf(_SC_GETPW_R_SIZE_MAX)
    if (bufSize == -1L) bufSize = 16384
    val buffer = allocArray<ByteVar>(bufSize)

    val res = getpwuid_r(uid, passwd.ptr, buffer, bufSize.toULong(), starResult.ptr)
    if (starResult.value == null) {
        // TODO: Check errno
        return null
    }

    return passwd
}
