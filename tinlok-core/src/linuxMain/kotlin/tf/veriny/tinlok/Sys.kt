/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.getpid
import platform.posix.getuid
import tf.veriny.tinlok.system.Syscall

/**
 * Linux-based system methods.
 */
public actual object Sys {
    public actual fun getenv(key: String): String? {
        return platform.posix.getenv(key)?.toKStringFromUtf8()
    }

    public actual fun getPID(): UInt {
        return getpid().toUInt()
    }

    @OptIn(Unsafe::class)
    public actual fun getUsername(): String? = memScoped {
        val passwd = Syscall.getpwuid_r(this, getuid())
        return passwd?.pw_name?.toKString()
    }

    public actual val osInfo: OsInfo = object : OsInfo {
        override val isPosix: Boolean get() = true
        override val isWindows: Boolean get() = false
        override val isLinux: Boolean get() = true
        override val isMacOs: Boolean get() = false
    }
}
