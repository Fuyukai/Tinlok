/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import tf.lotte.tinlok.Unsafe

// For SO_REUSEADDR, see: https://github.com/python-trio/trio/issues/928
// See also: https://github.com/python-trio/trio/issues/72

/**
 * Socket options that are shareable by all sockets.
 */
@OptIn(ExperimentalUnsignedTypes::class, Unsafe::class)
public object StandardSocketOptions {
    // https://sites.uclouvain.be/SystInfo/usr/include/asm-generic/socket.h.html

    // TODO: SO_LINGER high level
    /**
     * This option toggles recording of debugging information in the underlying protocol modules.
     */
    public val SO_DEBUG: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_DEBUG,
            level = SOL_SOCKET,
            name = "SO_DEBUG"
        )

    /**
     * This option allows a second application to re-bind to this port before the TIME_WAIT
     * period is up if this socket is ungracefully closed.
     */
    public val SO_REUSEADDR: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_REUSEADDR,
            level = SOL_SOCKET,
            name = "SO_REUSEADDR"
        )

    /**
     * This option controls whether the underlying protocol should periodically transmit messages
     * on a connected socket. If the peer fails to respond to these messages, the connection is
     * considered broken.
     */
    public val SO_KEEPALIVE: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_KEEPALIVE,
            level = SOL_SOCKET,
            name = "SO_KEEPALIVE"
        )

    /**
     * This option controls if broadcast packets can be sent over this socket. This has no effect
     * on IPv6 sockets.
     */
    public val SO_BROADCAST: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_BROADCAST,
            level = SOL_SOCKET,
            name = "SO_BROADCAST"
        )

    /**
     * If this option is set, out-of-band data received on the socket is placed in the normal input
     * queue.
     */
    public val SO_OOBINLINE: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_OOBINLINE,
            level = SOL_SOCKET,
            name = "SO_OOBINLINE"
        )

    /**
     * This option gets or sets the size of the output buffer.
     */
    public val SO_SNDBUF: BsdSocketOption<ULong> =
        ULongSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_SNDBUF  /* SO_SNDBUF */,
            level = SOL_SOCKET,
            name = "SO_OOBINLINE"
        )

    /**
     * This option gets or sets the size of the input buffer.
     */
    public val SO_RCVBUF: BsdSocketOption<ULong> =
        ULongSocketOption(
            bsdOptionValue = tf.lotte.tinlok.net.socket.SO_RCVBUF  /* SO_RCVBUF */,
            level = SOL_SOCKET,
            name = "SO_OOBINLINE"
        )


}
