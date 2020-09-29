/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.types.bytestring.ByteString

/**
 * Represents any object that is readable, consuming [ByteString] objects.
 */
public interface Writeable {
    /**
     * Writes the entirety of this [ByteString] to this object.
     */
    public fun writeAll(bs: ByteString)
}
