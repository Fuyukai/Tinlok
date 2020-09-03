/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs

import tf.lotte.knste.fs.path.Path
import tf.lotte.knste.io.Closeable
import tf.lotte.knste.io.Seekable
import tf.lotte.knste.io.StringReadable
import tf.lotte.knste.io.StringWriteable

/**
 * Represents a file on the filesystem.
 */
public interface FilesystemFile : StringReadable, Seekable, StringWriteable, Closeable {
    /** The path of this file. */
    public val path: Path
}
