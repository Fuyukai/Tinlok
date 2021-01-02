/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.tls

import tf.veriny.tinlok.Unsafe
import tf.veriny.tinlok.util.Closeable

/**
 * A [TlsContext] wraps an OpenSSL SSL_CTX, and produces new [TlsObject] instances with the
 * default config. This class *is* thread-safe; it can be used across multiple threads.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
@Unsafe
public expect class TlsContext(
    config: TlsContextConfiguration,
) : Closeable {
    /** The [TlsContextConfiguration] this context was created with. */
    public val config: TlsContextConfiguration

    /**
     * Creates a new [TlsObject] from this client context.
     * (This will fail if this is a server context).
     */
    public fun clientObject(hostname: String): TlsObject

    /**
     * Creates a new [TlsObject] from this server context.
     * (This will fail if this is a client context).
     */
    public fun serverObject(): TlsObject
}
