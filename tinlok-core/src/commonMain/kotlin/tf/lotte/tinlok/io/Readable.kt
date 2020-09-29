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
 * Represents any object that is readable, producing [ByteString] objects.
 */
public interface Readable {
    /**
     * Reads no more than [bytes] count bytes from this object.
     *
     * A null return means EOF.
     */
    public fun readUpTo(bytes: Long): ByteString?
}
