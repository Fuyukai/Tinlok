/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io.async

/**
 * Represents a half-closeable stream.
 *
 * This interface extends [AsyncBidirectionalStream] to also allow closing the send part of the
 * stream
 * without closing the receive part.
 */
public interface AsyncHalfCloseableStream : AsyncBidirectionalStream {
    /**
     * Sends an end-of-file indicator. This will close the writing half of the stream without
     * closing the reading half.
     *
     * Unlike [aclose], this method is NOT idempotent.
     */
    public suspend fun sendEof()
}
