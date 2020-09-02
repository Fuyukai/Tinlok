/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste

import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.*

/**
 * Linux-based system methods.
 */
public actual object System {
    public actual fun getenv(key: String): String? {
        return platform.posix.getenv(key)?.toKStringFromUtf8()
    }

    public actual fun getPID(): Int {
        return getpid()
    }

    public actual fun getUsername(): String = memScoped {
        getPasswdEntry(getuid())!!.pw_name!!.toKStringFromUtf8()
    }
}
