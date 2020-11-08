/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.dns

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.net.ConnectionInfo

/**
 * The global address resolver. This is used by default for all resolution requests without an
 * explicit resolver.
 */
public actual object GlobalResolver : AddressResolver {
    @Unsafe
    override fun getaddrinfo(
        host: String?, service: Int, family: AddressFamily, type: SocketType, protocol: IPProtocol,
        flags: Int,
    ): List<ConnectionInfo> {
        TODO("Not yet implemented")
    }
}
