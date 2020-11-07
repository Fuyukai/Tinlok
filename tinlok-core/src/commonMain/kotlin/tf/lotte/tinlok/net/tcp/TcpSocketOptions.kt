/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tcp

import tf.lotte.cc.Unsafe
import tf.lotte.cc.net.StandardIPProtocols
import tf.lotte.tinlok.net.socket.BooleanSocketOption
import tf.lotte.tinlok.net.socket.BsdSocketOption

/**
 * Namespace for TCP socket options.
 */
@OptIn(Unsafe::class)
public object TcpSocketOptions {
    private val IPPROTO_TCP = StandardIPProtocols.IPPROTO_TCP.number

    /**
     * Disables Nagle's algorithm which delays small writes.
     */
    public val TCP_NODELAY: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = 1  /* TCP_NODELAY */,
            level = IPPROTO_TCP,
            name = "TCP_NODELAY"
        )

    /**
     * Enables quick acknowledgement, where ACKs are sent immediately instead of delayed.
     */
    public val TCP_QUICKACK: BsdSocketOption<Boolean> =
        BooleanSocketOption(
            bsdOptionValue = 12  /* TCP_QUICKACK */,
            level = IPPROTO_TCP,
            name = "TCP_QUICKACK"
        )
}
