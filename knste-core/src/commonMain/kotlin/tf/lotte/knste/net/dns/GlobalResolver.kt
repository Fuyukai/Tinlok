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
import tf.lotte.knste.net.SocketKind
import tf.lotte.knste.util.Unsafe

/**
 * The global address resolver. This is used by default for all resolution requests without an
 * explicit resolver.
 */
public expect object GlobalResolver : AddressResolver
