/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KSTE.
 *
 * KSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.kste.io

import tf.lotte.kste.ByteString

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

    /**
     * Reads all bytes from this object.
     */
    public fun readAll(): ByteString
}
