/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net

/**
 * Base abstract class for all socket addresses.
 */
public abstract class SocketAddress<T : ConnectionInfo> : Set<T> {
    /** The hostname this address represents, or null otherwise. */
    public abstract val hostname: String?

    /** The protocol for this socket address. */
    public abstract val protocol: IPProtocol
}
