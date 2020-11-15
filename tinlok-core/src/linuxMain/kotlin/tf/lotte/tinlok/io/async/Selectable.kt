/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io.async

import tf.lotte.tinlok.system.FD

/**
 * Defines an object that can be selected on with a file descriptor.
 */
public actual interface Selectable {
    public val fd: FD
}
