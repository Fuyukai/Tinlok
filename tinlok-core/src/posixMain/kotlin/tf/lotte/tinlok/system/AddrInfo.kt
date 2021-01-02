/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.system

import tf.lotte.tinlok.net.IPAddress

/**
 * Copied result of an addrinfo or addrinfoW to allow more common code.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public data class AddrInfo(
    // this is copied into a ByteArray, because the actual addrinfo structures are de-allocated ASAP
    /** The raw bytes of the sockaddr. */
    public val addr: ByteArray,
    /** The canonical name. */
    public val canonname: String?,
    /** The address family. */
    public val family: Int,
    /** The flags. */
    public val flags: Int,
    /** The protocol. */
    public val protocol: Int,
    /** The socket type. */
    public val type: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddrInfo) return false

        if (!addr.contentEquals(other.addr)) return false
        if (canonname != other.canonname) return false
        if (family != other.family) return false
        if (flags != other.flags) return false
        if (protocol != other.protocol) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = addr.contentHashCode()
        result = 31 * result + canonname.hashCode()
        result = 31 * result + family.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + protocol.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

/**
 * Converts the [addr] inside this [AddrInfo] to an IP/Port combo.
 */
public expect fun AddrInfo.toIpPort(): Pair<IPAddress, Int>?
