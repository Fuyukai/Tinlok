/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import kotlin.native.concurrent.AtomicInt

/**
 * Implements an atomic boolean, using an [AtomicInt] underneath.
 */
public actual class AtomicBoolean actual constructor(initial: Boolean) {
    /** The backing int. */
    private val _value = AtomicInt(if (initial) 1 else 0)

    /** The actual value of this boolean. */
    public actual var value: Boolean
        get() = _value.value == 1
        set(value) {
            _value.value = if (value) 1 else 0
        }

    public override fun toString(): String = value.toString()

    /** Gets the inversion of this atomic boolean. */
    public actual operator fun not(): Boolean = _value.value == 0

    public actual fun compareAndSet(expected: Boolean, new: Boolean): Boolean {
        val realExpected = if (expected) 1 else 0
        val realNew = if (new) 1 else 0
        return _value.compareAndSet(realExpected, realNew)
    }
}
