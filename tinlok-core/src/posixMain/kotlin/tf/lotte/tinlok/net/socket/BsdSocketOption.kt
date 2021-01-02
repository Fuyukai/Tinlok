/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativePlacement
import tf.lotte.tinlok.Unsafe

/**
 * Sub-interface for BSD Socket API socket options. This is supported by both the BSD socket
 * interface on POSIX systems, and WinSock.
 */
public actual interface BsdSocketOption<T> : SocketOption<T> {
    /** The BSD option value, in int form. */
    public actual val bsdOptionValue: Int

    /** The level of this socket option. */
    public actual val level: Int

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

    /**
     * Gets the native size of this option (i.e. sizeof(allocateNativeStructure(...))).
     */
    public fun nativeSize(): Long
}
