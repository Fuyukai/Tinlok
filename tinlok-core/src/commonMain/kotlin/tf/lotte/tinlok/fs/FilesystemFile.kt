/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs

import tf.lotte.cc.io.BidirectionalStream
import tf.lotte.cc.io.Seekable
import tf.lotte.cc.types.ByteString
import tf.lotte.tinlok.fs.path.Path
import tf.lotte.tinlok.util.AtomicBoolean

/**
 * Represents a file on the filesystem.
 */
public interface FilesystemFile : BidirectionalStream, Seekable {
    /** If this file is still open. */
    public val isOpen: AtomicBoolean

    /** The path of this file. */
    public val path: Path

    /**
     * Reads all of the bytes of this file.
     */
    public fun readAll(): ByteString
}
