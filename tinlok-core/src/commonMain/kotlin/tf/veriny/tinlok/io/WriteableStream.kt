/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io

import tf.veriny.tinlok.util.Closeable

/**
 * Interface for any object that can be written to and closed (e.g. a socket).
 */
public interface WriteableStream : Writeable, Closeable
