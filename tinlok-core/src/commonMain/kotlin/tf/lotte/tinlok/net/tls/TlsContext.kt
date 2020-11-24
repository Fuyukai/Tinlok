/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.tls

import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.util.Closeable

/**
 * A [TlsContext] wraps an OpenSSL SSL_CTX, and produces new [TlsObject] instances with the
 * default config. This class *is* thread-safe; it can be used across multiple threads.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
@Unsafe
public expect class TlsContext(
    config: TlsConnectionConfig
) : Closeable {
    /** The [TlsConnectionConfig] this context was created with. */
    public val config: TlsConnectionConfig
}
