/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.cc.exc.OSException

/**
 * Thrown when a exception happpens relating to TLS code.
 */
public class TlsException(override val message: String) : OSException(errno = -1)
