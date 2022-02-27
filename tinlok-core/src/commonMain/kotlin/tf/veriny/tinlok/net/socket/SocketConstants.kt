/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.socket

/* Various socket constants that differ by platform. */

// == Address families == //
internal expect val AF_UNSPEC: Int
internal expect val AF_INET: Int
internal expect val AF_INET6: Int
internal expect val AF_UNIX: Int

// == Socket types == //
internal expect val SOCK_STREAM: Int
internal expect val SOCK_DGRAM: Int
internal expect val SOCK_RAW: Int

// == Protocol types == //
internal expect val IPPROTO_IP: Int
internal expect val IPPROTO_ICMP: Int
internal expect val IPPROTO_TCP: Int
internal expect val IPPROTO_UDP: Int

// == SOL_SOCKET options == //
internal expect val SOL_SOCKET: Int
internal expect val SO_DEBUG: Int
internal expect val SO_REUSEADDR: Int
internal expect val SO_KEEPALIVE: Int
internal expect val SO_BROADCAST: Int
internal expect val SO_OOBINLINE: Int
internal expect val SO_SNDBUF: Int
internal expect val SO_RCVBUF: Int
