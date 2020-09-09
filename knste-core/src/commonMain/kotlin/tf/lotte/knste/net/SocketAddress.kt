/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net

/**
 * Base abstract class for all socket addresses.
 */
public abstract class SocketAddress(
    /** The AF_ address family for this address. */
    public val family: AddressFamily,
    /** The SOCK_ socket kind for this address. */
    public val kind: SocketKind,
    /** The actual IP address. */
    public val ipAddress: IPAddress,
) {
}
