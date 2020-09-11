/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.io

/**
 * Interface for any object that can be read off of and closed (e.g. a socket).
 */
public interface ReadableStream : Readable, Closeable
