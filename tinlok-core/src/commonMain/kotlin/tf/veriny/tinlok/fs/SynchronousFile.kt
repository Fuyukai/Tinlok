/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.fs

import tf.veriny.tinlok.fs.path.Path
import tf.veriny.tinlok.io.BidirectionalStream
import tf.veriny.tinlok.io.Seekable
import tf.veriny.tinlok.util.AtomicBoolean

/**
 * A synchronous file on a filesystem.
 */
public interface SynchronousFile : BidirectionalStream, Seekable {
    /** If this file is still open. */
    public val isOpen: AtomicBoolean

    /** The path of this file. */
    public val path: Path

    /**
     * Synchronises the filesystem for this file. If ``full``, then all metadata will be flushed;
     * otherwise, only metadata needed for subsequent data retrieval will be flushed.
     */
    public fun sync(full: Boolean = false)
}
