/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.cc

/**
 * A scope that can have objects added to it to automatically close them.
 *
 * This is an interface so that classes can easily inherit (and delegate) to it.
 */
public interface ClosingScope : Closeable {
    public companion object {
        /**
         * Opens a new scope, runs [block] in it, then closes it.
         */
        @OptIn(Unsafe::class)
        public inline operator fun <R> invoke(block: (ClosingScope) -> R): R {
            return ClosingScopeImpl().use(block)
        }
    }

    /** Adds a closeable to this scope, that will be closed when this scope is also closed. */
    public fun add(closeable: Closeable)
}

@PublishedApi
internal class ClosingScopeImpl @Unsafe constructor() : ClosingScope {
    private val toClose = mutableSetOf<Closeable>()

    override fun add(closeable: Closeable) {
        toClose.add(closeable)
    }

    override fun close() {
        var lastException: Throwable? = null
        for (item in toClose) {
            try {
                item.close()
            } catch (e: Throwable) {
                lastException = e
            }
        }

        if (lastException != null) throw lastException
    }
}