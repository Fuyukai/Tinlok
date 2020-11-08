/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.net.socket

import kotlinx.cinterop.*
import tf.lotte.tinlok.Unsafe

/**
 * A socket option for a boolean.
 */
public actual class BooleanSocketOption actual constructor(
    actual override val bsdOptionValue: Int,
    override val level: Int,
    override val name: String
) : BsdSocketOption<Boolean> {

    @Unsafe
    override fun allocateNativeStructure(allocator: NativePlacement): CPointer<*> {
        return allocator.alloc<IntVar>().ptr
    }

    @Unsafe
    override fun toNativeStructure(
        allocator: NativePlacement, value: Boolean,
    ): CPointer<IntVar> {
        // we just allocate an int (lol)
        // somebody may be able to correct this in the future.
        // a lot of the CValue classes etc are really confusing.
        val int = allocator.alloc<IntVar>()
        int.value = if (value) 1 else 0
        return int.ptr
    }

    @Unsafe
    override fun fromNativeStructure(
        allocator: NativePlacement, structure: CPointer<*>,
    ): Boolean {
        // THIS CORRUPTS MEMORY IF THIS CAST FAILS
        // DO NOT PASS THIS FUNCTION THINGS IT DOESN'T EXPECT!
        val int = (structure as CPointer<IntVar>).pointed.value
        return int != 0
    }

    override fun nativeSize(): Long {
        return sizeOf<IntVar>()
    }
}

/**
 * A socket option for an unsigned long.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public actual class ULongSocketOption actual constructor(
    actual override val bsdOptionValue: Int,
    override val level: Int,
    override val name: String
) : BsdSocketOption<ULong> {
    @Unsafe
    override fun allocateNativeStructure(allocator: NativePlacement): CPointer<*> {
        return allocator.alloc<ULongVar>().ptr
    }

    @Unsafe
    override fun toNativeStructure(
        allocator: NativePlacement, value: ULong,
    ): CPointer<ULongVar> {
        val long = allocator.alloc<ULongVar>()
        long.value = value
        return long.ptr
    }

    @Unsafe
    override fun fromNativeStructure(
        allocator: NativePlacement, structure: CPointer<*>,
    ): ULong {
        // THIS EQUALLY CORRUPTS MEMORY!!!!
        return (structure as CPointer<ULongVar>).pointed.value
    }

    override fun nativeSize(): Long = sizeOf<ULongVar>()
}
