/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.util

/**
 * A superclass that provides helper behaviour for atomic closeables. Instances of this class are
 * assumed to start open (change _isOpen in init {} if this is not the case).
 */
public abstract class AtomicSafeCloseable : Closeable {
    /**
     * The toggleable boolean for this safe closeable.
     */
    protected val _isOpen: AtomicBoolean = AtomicBoolean(true)

    /**
     * Checks if this object is still open.
     */
    protected fun checkOpen() {
        if (!_isOpen) throw ClosedException("this object is closed")
    }

    /**
     * Implements actual close logic. Will only ever be ran once.
     */
    protected abstract fun closeImpl()

    final override fun close() {
        if (!_isOpen.compareAndSet(expected = true, new = false)) return
        closeImpl()
    }
}
