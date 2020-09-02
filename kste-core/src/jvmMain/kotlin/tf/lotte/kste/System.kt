/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste

import java.lang.System

/**
 * JVM implementation of the System object.
 */
public actual object System {
    public actual fun getenv(key: String): String? {
        return System.getenv(key)
    }

    public actual fun getPID(): Int {
        return ProcessHandle.current().pid().toInt()
    }

    public actual fun getUsername(): String {
        return System.getProperty("user.name")
    }
}
