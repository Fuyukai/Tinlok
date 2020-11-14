/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.util

import kotlinx.cinterop.*
import tf.lotte.tinlok.Unsafe
import tf.lotte.tinlok.io.FdSelectionKey
import tf.lotte.tinlok.io.async.Selectable
import tf.lotte.tinlok.io.async.SelectionKey
import tf.lotte.tinlok.system.BlockingResult
import tf.lotte.tinlok.system.FD
import tf.lotte.tinlok.system.Syscall

/**
 * A wrapper around an event file descriptor.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class EventFD @Unsafe public constructor(vararg flag: Flag) : Closeable, Selectable {
    /** An enumeration of valid flags to open this EventFD with. */
    public enum class Flag(public val number: Int) {
        /** Close-on-exec. */
        EFD_CLOEXEC(platform.linux.EFD_CLOEXEC),
        /** Non-blocking. (See: O_NONBLOCK). */
        EFD_NONBLOCK(platform.linux.EFD_NONBLOCK),
        /** Provides semaphore-like semantics for reads. */
        EFD_SEMAPHORE(platform.linux.EFD_SEMAPHORE)
    }

    public companion object {
        /**
         * Creates a new [EventFD] and passes it to [block].
         */
        @OptIn(Unsafe::class)
        public operator fun <R> invoke(vararg flag: Flag, block: (EventFD) -> R): R {
            val fd = EventFD(*flag)
            return fd.use(block)
        }

        /**
         * Creates a new [EventFD], adds it to [scope], and returns it.
         */
        @OptIn(Unsafe::class)
        public fun within(scope: ClosingScope, vararg flag: Flag): EventFD {
            val fd = EventFD(*flag)
            scope.add(fd)
            return fd
        }
    }

    /** If this eventfd is open. */
    private val isOpen = AtomicBoolean(true)

    private fun checkOpen() {
        if (!isOpen) throw ClosedException("This eventfd is closed")
    }

    /** The underlying file descriptor for this event fd. */
    public val fd: FD

    init {
        val flagArr = flag.map { it.number }.toIntArray()
        val flagBits = flags(*flagArr)

        @OptIn(Unsafe::class)
        fd = Syscall.eventfd(0u, flagBits)
    }

    /**
     * Closes this resource.
     *
     * This method is idempotent; subsequent calls will have no effects.
     */
    @OptIn(Unsafe::class)
    override fun close() {
        if (!isOpen) return

        Syscall.close(fd)
    }

    /**
     * Gets the selection key for this selectable.
     */
    override fun key(): SelectionKey {
        return FdSelectionKey(fd)
    }

    /**
     * Reads the value of this event fd.
     */
    @OptIn(Unsafe::class)
    public fun read(): BlockingResult {
        return memScoped {
            val cnt = alloc<ULongVar>()
            Syscall.read(fd, cnt.ptr.reinterpret(), 8)
        }
    }

    /**
     * Sets the value of this event fd.
     */
    @OptIn(Unsafe::class)
    public fun write(value: ULong): BlockingResult {
        return memScoped {
            val cnt = alloc<ULongVar>()
            cnt.value = value
            Syscall.write(fd, cnt.ptr.reinterpret(), 8)
        }
    }
}
