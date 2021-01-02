/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok

/**
 * Semantically similar to Java's System namespace. Contains several useful functions for
 * interacting with the current operating system.
 */
public expect object Sys {
    /**
     * Gets an environmental variable named [key].
     */
    public fun getenv(key: String): String?

    /**
     * Gets the current process ID.
     */
    @OptIn(ExperimentalUnsignedTypes::class)
    public fun getPID(): UInt

    /**
     * Gets the current username of the user running this application.
     */
    public fun getUsername(): String?

    /** Information object about the current OS. */
    public val osInfo: OsInfo
}
