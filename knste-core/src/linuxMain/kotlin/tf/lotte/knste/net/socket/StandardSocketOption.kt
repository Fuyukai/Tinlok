/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.lotte.knste.net.socket

import kotlinx.cinterop.NativePlacement
import platform.posix.SOL_SOCKET
import tf.lotte.knste.util.Unsafe

/**
 * Socket options that are shareable by all sockets.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public actual sealed class StandardSocketOption<T>(override val name: String) :
    SocketOption<T>, LinuxSocketOption<T> {
    private class BooleanSocketOption(name: String) : StandardSocketOption<Boolean>(name) {
        @Unsafe
        override fun toNativeStructure(allocator: NativePlacement, value: Boolean): Any {
            return if (value) 1 else 0
        }
    }

    private class ULongSocketOption(name: String) : StandardSocketOption<ULong>(name) {
        @Unsafe
        override fun toNativeStructure(allocator: NativePlacement, value: ULong): Any {
            return value
        }
    }

    // all StandardSocketOption's are SOL_SOCKET
    override val level: Int get() = SOL_SOCKET

    public actual companion object {

        // TODO: SO_LINGER high level
        /**
         * This option toggles recording of debugging information in the underlying protocol modules.
         */
        public actual val SO_DEBUG: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_DEBUG")

        /**
         * This option allows a second application to re-bind to this port before the TIME_WAIT
         * period is up if this socket is ungracefully closed.
         */
        public actual val SO_REUSEADDR: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_DEBUG")

        /**
         * This option controls whether the underlying protocol should periodically transmit messages
         * on a connected socket. If the peer fails to respond to these messages, the connection is
         * considered broken.
         */
        public actual val SO_KEEPALIVE: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_KEEPALIVE")

        /**
         * This option controls if broadcast packets can be sent over this socket. This has no effect
         * on IPv6 sockets.
         */
        public actual val SO_BROADCAST: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_BROADCAST")

        /**
         * If this option is set, out-of-band data received on the socket is placed in the normal input
         * queue.
         */
        public actual val SO_OOBINLINE: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_OOBINLINE")

        /**
         * This option gets or sets the size of the output buffer.
         */
        public actual val SO_SNDBUF: StandardSocketOption<ULong>
            = ULongSocketOption("SO_SNDBUF")

        /**
         * This option gets or sets the size of the input buffer.
         */
        public actual val SO_RCVBUF: StandardSocketOption<ULong>
            = ULongSocketOption("SO_RCVBUF")

    }
}
