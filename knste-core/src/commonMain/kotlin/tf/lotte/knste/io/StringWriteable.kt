/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.io

import tf.lotte.knste.ByteString

/**
 * Represents a [Writeable] that you can also write regular strings to.
 */
public interface StringWriteable : Writeable {
    public fun writeString(str: String) {
        writeAll(ByteString.fromString(str))
    }
}
