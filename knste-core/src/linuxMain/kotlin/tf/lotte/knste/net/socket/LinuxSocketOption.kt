/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.net.socket

import kotlinx.cinterop.NativePlacement
import tf.lotte.knste.util.Unsafe

/**
 * Sub-interface for linux socket options.
 */
public interface LinuxSocketOption<T> : SocketOption<T> {
    /**
     * Converts this option to a native platform structure, using [allocator] if needed.
     */
    @Unsafe
    public fun toNativeStructure(allocator: NativePlacement, value: T): Any
}
