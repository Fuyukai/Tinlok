/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

/**
 * A server socket that can be bound to an address.
 *
 * All [ServerSocket] instances wrap the address they bind to.
 */
public interface ServerSocket : Socket {
    /**
     * Binds this socket to its enclosed address.
     */
    public fun bind(backlog: Int = 128)  // old linux support for 128
}
