/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.getpid
import platform.posix.getuid

/**
 * Linux-based system methods.
 */
public actual object Sys {
    public actual fun getenv(key: String): String? {
        return platform.posix.getenv(key)?.toKStringFromUtf8()
    }

    public actual fun getPID(): Int {
        return getpid()
    }

    public actual fun getUsername(): String = memScoped {
        getPasswdEntry(getuid())!!.pw_name!!.toKStringFromUtf8()
    }

    public actual val osInfo: OsInfo = object : OsInfo {
        override val isPosix: Boolean get() = true
        override val isWindows: Boolean get() = false
        override val isLinux: Boolean get() = true
        override val isMacOs: Boolean get() = false
    }
}
