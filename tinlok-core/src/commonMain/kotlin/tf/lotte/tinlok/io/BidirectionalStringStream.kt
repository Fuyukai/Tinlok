/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

// xx: not sure how to format this

/**
 * Represents a two-directional stream that can read strings.
 */
public interface BidirectionalStringStream
    :
    StringReadableStream,
    StringWriteableStream,
    BidirectionalStream
