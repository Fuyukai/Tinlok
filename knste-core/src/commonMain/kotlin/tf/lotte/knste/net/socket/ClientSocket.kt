/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.socket

import tf.lotte.knste.io.HalfCloseableStream

/**
 * A client socket that performs I/O synchronously.
 *
 * @param ADDR: The type of address this client socket uses.
 */
public interface ClientSocket<ADDR : SocketAddress<*>> : HalfCloseableStream, Socket {
    /**
     * Connects this socket to the remote [address].
     */
    public fun connect(address: ADDR)
}
