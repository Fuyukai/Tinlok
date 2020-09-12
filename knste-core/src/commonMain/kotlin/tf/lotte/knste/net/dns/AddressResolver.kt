/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.dns

import tf.lotte.knste.net.AddressFamily
import tf.lotte.knste.net.IPProtocol
import tf.lotte.knste.net.SocketAddress
import tf.lotte.knste.net.SocketType
import tf.lotte.knste.util.Unsafe

/**
 * Interface for address resolvers. This defines a portable getaddrinfo()-like high level function.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public interface AddressResolver {
    // gross boxed type alert with port
    /**
     * Gets address information for the specified inputs. This is a very low-level API; you
     * almost definitely do not want to use it.
     *
     * The specified host will already be IDNA encoded, so it is safe to perform things like AAAA
     * lookups directly.
     *
     * @param host: The hostname to look up. If null, will do a localhost lookup with AI_PASSIVE.
     * @param service: The service port to look up. Optional.
     * @param family: The [AddressFamily] to use. Defaults to UNSPEC (use both).
     * @param type: The [SocketType] to use. Defaults to STREAM (TCP).
     * @param protocol: The [IPProtocol] to use. Defaults to kernel choice.
     * @param flags: The flags to use.
     */
    @Unsafe
    public fun getaddrinfo(
        host: String?, service: Int = 0,
        family: AddressFamily = AddressFamily.AF_UNSPEC,
        type: SocketType = SocketType.SOCK_STREAM,
        protocol: IPProtocol = IPProtocol.IPPROTO_IP,
        flags: Int = 0
    ): List<SocketAddress>
}
