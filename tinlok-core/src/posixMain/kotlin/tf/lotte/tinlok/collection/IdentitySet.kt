/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.collection

import kotlin.native.identityHashCode

// TODO: Ask JB for the ability to override hashes.
// Alternatively, implement our own HashSet, which allows it.

/**
 * An implementation of an IdentitySet using a wrapper class.
 */
public actual class IdentitySet<E> public actual constructor() : MutableSet<E> {
    /** The backing hash set for this IdentitySet. */
    private val backing = LinkedHashSet<IdentityWrapper>()

    /**
     * The actual wrapper class that is stored in the set.
     */
    private inner class IdentityWrapper(val item: E) {
        override fun hashCode(): Int {
            return item.identityHashCode()
        }

        override fun equals(other: Any?): Boolean {
            return (other is IdentitySet<*>.IdentityWrapper) && other.item == item
        }
    }

    /**
     * An iterator that wraps and unwraps the object wrappers.
     */
    private inner class WrapperIterator : MutableIterator<E> {
        val realIterator = backing.iterator()

        override fun hasNext(): Boolean {
            return realIterator.hasNext()
        }

        override fun next(): E {
            val wrapper = realIterator.next()
            return wrapper.item
        }

        override fun remove() {
            return realIterator.remove()
        }
    }


    // delegates to the actual set
    override val size: Int get() = backing.size
    override fun clear(): Unit = backing.clear()
    override fun isEmpty(): Boolean = backing.isEmpty()

    override fun add(element: E): Boolean {
        val wrapper = IdentityWrapper(element)
        return backing.add(wrapper)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val wrappers = elements.map { IdentityWrapper(it) }
        return backing.addAll(wrappers)
    }

    override fun contains(element: E): Boolean {
        val wrapper = IdentityWrapper(element)
        return backing.contains(wrapper)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        val wrappers = elements.map { IdentityWrapper(it) }
        return backing.containsAll(wrappers)
    }

    override fun iterator(): MutableIterator<E> = WrapperIterator()

    override fun remove(element: E): Boolean {
        val wrapper = IdentityWrapper(element)
        return backing.remove(wrapper)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val wrappers = elements.map { IdentityWrapper(it) }
        return backing.removeAll(wrappers)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        val wrappers = elements.map { IdentityWrapper(it) }
        return backing.retainAll(wrappers)
    }

}
