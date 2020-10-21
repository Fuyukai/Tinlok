/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok

import platform.windows.GetCurrentProcessId
import tf.lotte.tinlok.system.Syscall
import tf.lotte.tinlok.util.Unsafe

/**
 * Semantically similar to Java's System namespace. Contains several useful functions for
 * interacting with the current operating system.
 */
public actual object Sys {
    /**
     * Gets an environmental variable named [key].
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    actual fun getenv(key: String): String? {

    }

    /**
     * Gets the current process ID.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    actual fun getPID(): UInt {
        return GetCurrentProcessId()
    }

    /**
     * Gets the current username of the user running this application.
     */
    @OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
    actual fun getUsername(): String? = Syscall.GetUserName()

    /** Information object about the current OS. */
    actual val osInfo: OsInfo = object : OsInfo {
        override val isLinux: Boolean get() = false
        override val isMacOs: Boolean get() = false
        override val isPosix: Boolean get() = true
        override val isWindows: Boolean get() = true
    }

}
