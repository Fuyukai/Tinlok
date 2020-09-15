/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("unused")

package tf.lotte.tinlok.net.socket

import kotlinx.cinterop.*
import platform.posix.SOL_SOCKET
import tf.lotte.tinlok.util.Unsafe

/**
 * Socket options that are shareable by all sockets.
 */
@OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
public actual sealed class StandardSocketOption<T>(override val name: String) :
    SocketOption<T>, LinuxSocketOption<T> {

    @Unsafe
    private class BooleanSocketOption(
        name: String, override val linuxOptionName: Int
    ) : StandardSocketOption<Boolean>(name) {
        override fun allocateNativeStructure(allocator: NativePlacement): CPointer<*> {
            return allocator.alloc<IntVar>().ptr
        }

        override fun toNativeStructure(
            allocator: NativePlacement, value: Boolean
        ): CPointer<IntVar> {
            // we just allocate an int (lol)
            // somebody may be able to correct this in the future.
            // a lot of the CValue classes etc are really confusing.
            val int = allocator.alloc<IntVar>()
            int.value  = if (value) 1 else 0
            return int.ptr
        }

        override fun fromNativeStructure(allocator: NativePlacement, structure: CPointer<*>): Boolean {
            // THIS CORRUPTS MEMORY IF THIS CAST FAILS
            // DO NOT PASS THIS FUNCTION THINGS IT DOESN'T EXPECT!
            val int = (structure as CPointer<IntVar>).pointed.value
            return int != 0
        }

        override fun nativeSize(): Long {
            return sizeOf<IntVar>()
        }
    }

    @Unsafe
    private class ULongSocketOption(
        name: String, override val linuxOptionName: Int
    ) : StandardSocketOption<ULong>(name) {
        override fun allocateNativeStructure(allocator: NativePlacement): CPointer<*> {
            return allocator.alloc<ULongVar>().ptr
        }

        @Unsafe
        override fun toNativeStructure(
            allocator: NativePlacement, value: ULong
        ): CPointer<ULongVar> {
            val long = allocator.alloc<ULongVar>()
            long.value = value
            return long.ptr
        }

        @Unsafe
        override fun fromNativeStructure(allocator: NativePlacement, structure: CPointer<*>): ULong {
            // THIS EQUALLY CORRUPTS MEMORY!!!!
            return (structure as CPointer<ULongVar>).pointed.value
        }

        override fun nativeSize(): Long = sizeOf<ULongVar>()
    }

    // all StandardSocketOption's are SOL_SOCKET
    override val level: Int get() = SOL_SOCKET

    // https://sites.uclouvain.be/SystInfo/usr/include/asm-generic/socket.h.html
    public actual companion object {

        // TODO: SO_LINGER high level
        /**
         * This option toggles recording of debugging information in the underlying protocol modules.
         */
        public actual val SO_DEBUG: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_DEBUG", 1  /* SO_DEBUG */)

        /**
         * This option allows a second application to re-bind to this port before the TIME_WAIT
         * period is up if this socket is ungracefully closed.
         */
        public actual val SO_REUSEADDR: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_REUSEADDR", 2  /* SO_REUSEADDR */)

        /**
         * This option controls whether the underlying protocol should periodically transmit messages
         * on a connected socket. If the peer fails to respond to these messages, the connection is
         * considered broken.
         */
        public actual val SO_KEEPALIVE: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_KEEPALIVE", 9  /* SO_KEEPALIVE */)

        /**
         * This option controls if broadcast packets can be sent over this socket. This has no effect
         * on IPv6 sockets.
         */
        public actual val SO_BROADCAST: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_BROADCAST", 6  /* SO_BROADCAST */)

        /**
         * If this option is set, out-of-band data received on the socket is placed in the normal input
         * queue.
         */
        public actual val SO_OOBINLINE: StandardSocketOption<Boolean>
            = BooleanSocketOption("SO_OOBINLINE", 10  /* SO_OOBINLINE */)

        /**
         * This option gets or sets the size of the output buffer.
         */
        public actual val SO_SNDBUF: StandardSocketOption<ULong>
            = ULongSocketOption("SO_SNDBUF", 7  /* SO_SNDBUF */)

        /**
         * This option gets or sets the size of the input buffer.
         */
        public actual val SO_RCVBUF: StandardSocketOption<ULong>
            = ULongSocketOption("SO_RCVBUF", 8  /* SO_RCVBUF */)

    }
}
