/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePlacement
import tf.lotte.tinlok.util.Unsafe

/**
 * Sub-interface for linux socket options.
 */
public interface LinuxSocketOption<T> : SocketOption<T> {
    /** The Linux option name, in int form. */
    public val linuxOptionName: Int

    /**
     * Allocates an empty (zero-value) native structure.
     */
    @Unsafe
    public fun allocateNativeStructure(allocator: NativePlacement): CPointer<*>

    /**
     * Converts this option to a native platform structure, using [allocator] if needed.
     */
    @Unsafe
    public fun toNativeStructure(allocator: NativePlacement, value: T): CPointer<*>

    /**
     * Converts this option from a native platform [structure], using [allocator] if needed.
     */
    @Unsafe
    public fun fromNativeStructure(allocator: NativePlacement, structure: CPointer<*>): T

    @Unsafe
    public fun nativeSize(): Long
}
