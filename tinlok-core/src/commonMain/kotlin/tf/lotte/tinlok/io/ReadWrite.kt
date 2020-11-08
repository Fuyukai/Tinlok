/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.util.Closeable

/**
 * Interface for any object that is both [Readable] and [Writeable], but not [Closeable].
 */
public interface ReadWrite : Readable, Writeable
