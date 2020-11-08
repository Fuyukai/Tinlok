/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.io.OSException

/**
 * Thrown when a exception happpens relating to TLS code.
 */
public class TlsException
public constructor(
    message: String, cause: Throwable?,
) : OSException(message, cause) {
    public constructor(message: String) : this(message, null)
}
