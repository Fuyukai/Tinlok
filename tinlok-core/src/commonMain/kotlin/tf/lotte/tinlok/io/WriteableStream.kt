/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

/**
 * Interface for any object that can be written to or closed (e.g. a socket).
 */
public interface WriteableStream : Writeable, Closeable
