/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.ByteString

/**
 * Represents a [Writeable] that you can also write regular strings to.
 */
public interface StringWriteable : Writeable {
    public fun writeString(str: String) {
        writeAll(ByteString.fromString(str))
    }
}
