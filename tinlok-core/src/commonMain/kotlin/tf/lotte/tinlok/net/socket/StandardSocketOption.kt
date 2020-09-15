/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

// For SO_REUSEADDR, see: https://github.com/python-trio/trio/issues/928
// See also: https://github.com/python-trio/trio/issues/72

/**
 * A standard socket option (SOL_SOCKET).
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect sealed class StandardSocketOption<T> : SocketOption<T> {
    public companion object {
        /**
         * This option toggles recording of debugging information in the underlying protocol modules.
         */
        public val SO_DEBUG: StandardSocketOption<Boolean>

        /**
         * This option allows a second application to re-bind to this port before the TIME_WAIT
         * period is up if this socket is ungracefully closed.
         */
        public val SO_REUSEADDR: StandardSocketOption<Boolean>

        /**
         * This option controls whether the underlying protocol should periodically transmit messages
         * on a connected socket. If the peer fails to respond to these messages, the connection is
         * considered broken.
         */
        public val SO_KEEPALIVE: StandardSocketOption<Boolean>

        // TODO: SO_LINGER high level

        /**
         * This option controls if broadcast packets can be sent over this socket. This has no effect
         * on IPv6 sockets.
         */
        public val SO_BROADCAST: StandardSocketOption<Boolean>

        /**
         * If this option is set, out-of-band data received on the socket is placed in the normal input
         * queue.
         */
        public val SO_OOBINLINE: StandardSocketOption<Boolean>

        /**
         * This option gets or sets the size of the output buffer.
         */
        public val SO_SNDBUF: StandardSocketOption<ULong>

        /**
         * This option gets or sets the size of the input buffer.
         */
        public val SO_RCVBUF: StandardSocketOption<ULong>
    }
}
