/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

import tf.lotte.tinlok.io.async.SelectionKey
import tf.lotte.tinlok.system.FD

/**
 * A selection key based on a file descriptor.
 */
public inline class FdSelectionKey(public val fd: FD) : SelectionKey
