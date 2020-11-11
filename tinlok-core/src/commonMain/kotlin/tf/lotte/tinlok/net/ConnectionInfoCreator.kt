/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

/**
 * Defines a connection information creator, to turn an IP address into a ConnectionInfo.
 */
public fun interface ConnectionInfoCreator<I : ConnectionInfo> {
    /**
     * Creates a new [I] from the IP address and port specified.
     */
    public fun from(ip: IPAddress, port: Int): I
}
