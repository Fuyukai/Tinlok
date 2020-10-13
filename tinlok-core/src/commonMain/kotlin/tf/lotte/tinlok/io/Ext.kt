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
 * Peeks no more than the specified number of bytes without advancing the cursor position.
 */
public fun <T> T.peek(count: Long): ByteString?
    where T : Readable, T : Seekable {
    val cursorBefore = cursor()
    val bs = readUpTo(count) ?: return null
    this.seekAbsolute(cursorBefore)
    return bs
}


