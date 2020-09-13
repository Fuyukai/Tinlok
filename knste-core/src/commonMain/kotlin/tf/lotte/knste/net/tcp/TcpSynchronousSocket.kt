/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.tcp

import tf.lotte.knste.io.HalfCloseableStream
import tf.lotte.knste.net.socket.Socket

/**
 * Interface defining a TCP synchronous socket.
 */
public interface TcpSynchronousSocket : HalfCloseableStream, Socket
