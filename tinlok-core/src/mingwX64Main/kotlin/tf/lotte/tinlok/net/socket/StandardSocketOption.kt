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
 * A standard socket option (SOL_SOCKET).
 */
public actual sealed class StandardSocketOption<T> : SocketOption<T> {
    public actual companion object {

        // TODO: SO_LINGER high level
        /**
         * This option toggles recording of debugging information in the underlying protocol modules.
         */
        public actual val SO_DEBUG: StandardSocketOption<Boolean>
            get() = TODO("Not yet implemented")

        /**
         * This option allows a second application to re-bind to this port before the TIME_WAIT
         * period is up if this socket is ungracefully closed.
         */
        public actual val SO_REUSEADDR: StandardSocketOption<Boolean>
            get() = TODO("Not yet implemented")

        /**
         * This option controls whether the underlying protocol should periodically transmit messages
         * on a connected socket. If the peer fails to respond to these messages, the connection is
         * considered broken.
         */
        public actual val SO_KEEPALIVE: StandardSocketOption<Boolean>
            get() = TODO("Not yet implemented")

        /**
         * This option controls if broadcast packets can be sent over this socket. This has no effect
         * on IPv6 sockets.
         */
        public actual val SO_BROADCAST: StandardSocketOption<Boolean>
            get() = TODO("Not yet implemented")

        /**
         * If this option is set, out-of-band data received on the socket is placed in the normal input
         * queue.
         */
        public actual val SO_OOBINLINE: StandardSocketOption<Boolean>
            get() = TODO("Not yet implemented")

        /**
         * This option gets or sets the size of the output buffer.
         */
        public actual val SO_SNDBUF: StandardSocketOption<ULong>
            get() = TODO("Not yet implemented")

        /**
         * This option gets or sets the size of the input buffer.
         */
        public actual val SO_RCVBUF: StandardSocketOption<ULong>
            get() = TODO("Not yet implemented")

    }
}