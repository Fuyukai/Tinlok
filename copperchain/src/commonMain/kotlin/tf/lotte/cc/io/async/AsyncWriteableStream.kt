/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc.io.async

/**
 * Interface for any object that can be asynchronously written to and closed (e.g. a socket).
 */
public interface AsyncWriteableStream : AsyncWriteable, AsyncCloseable
