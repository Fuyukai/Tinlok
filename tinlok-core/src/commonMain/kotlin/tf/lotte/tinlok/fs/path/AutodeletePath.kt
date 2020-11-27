/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.util.AtomicSafeCloseable

/**
 * A path that will automatically delete itself when closed.
 */
public class AutodeletePath(realPath: Path) : Path by realPath, AtomicSafeCloseable() {
    override fun closeImpl() {
        delete()
    }
}
