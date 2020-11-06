/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net

import tf.lotte.cc.net.SocketAddress

/**
 * Thrown when an attempt to connect a socket fails with all possible connections.
 */
public class AllConnectionsFailedException(
    public val addr: SocketAddress<*>, cause: Throwable? = null,
) : Exception("Connecting to $addr failed on all attempts", cause)
