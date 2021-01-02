/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.io.async

import tf.veriny.tinlok.io.ReadWrite

/**
 * Asynchronous version of [ReadWrite].
 */
public interface AsyncReadWrite : AsyncReadable, AsyncWriteable
