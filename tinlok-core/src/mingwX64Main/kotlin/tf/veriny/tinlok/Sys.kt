/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok

import platform.windows.GetCurrentProcessId
import tf.veriny.tinlok.system.Syscall

/**
 * Semantically similar to Java's System namespace. Contains several useful functions for
 * interacting with the current operating system.
 */
public actual object Sys {
    /**
     * Gets an environmental variable named [key].
     */
    @OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
    public actual fun getenv(key: String): String? {
        return Syscall.GetEnvironmentVariable(key)
    }

    /**
     * Gets the current process ID.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public actual fun getPID(): UInt {
        return GetCurrentProcessId()
    }

    /**
     * Gets the current username of the user running this application.
     */
    @OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
    public actual fun getUsername(): String? = Syscall.GetUserName()

    /** Information object about the current OS. */
    public actual val osInfo: OsInfo = object : OsInfo {
        override val isLinux: Boolean get() = false
        override val isMacOs: Boolean get() = false
        override val isPosix: Boolean get() = true
        override val isWindows: Boolean get() = true
    }

}
