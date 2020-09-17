/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.tinlok.*
import tf.lotte.tinlok.util.Unsafe

/**
 * A pure path that uses POSIX semantics.
 */
public open class PosixPurePath(rawParts: List<ByteString>) : PurePath {
    public companion object {
        private val SLASH = b("/")
        private val DOT = b(".")
        private val DOTDOT = b("..")

        // singleton instances
        internal val SLASH_PATH = PosixPurePath(listOf(SLASH))
        internal val DOT_PATH = PosixPurePath(listOf(DOT))

        /**
         * Creates a new [PosixPurePath] from a string.
         */
        public fun fromString(s: String): PosixPurePath {
            return fromByteString(s.toByteString())
        }

        /**
         * Creates a new [PosixPurePath] from a [ByteString].
         */
        public fun fromByteString(s: ByteString): PosixPurePath {
            // this contains the bulk of the path processing logic
            val parts = ArrayDeque<ByteString>()

            val isAbsolute = s.startsWith(SLASH)
            if (isAbsolute) {
                parts.addLast(SLASH)
            }

            val split = s.split(SLASH)

            for (part in split) {
                // empty parts (i.e. double slashes, usually) are ignored entirely
                if (part.isEmpty()) continue
                // `.` is ignored entirely
                if (part == DOT) continue
                // `..` is just added on
                // this is explicit so that the logic can be changed easily in the future.
                else if (part == DOTDOT) parts.addLast(DOTDOT)
                // everything else is just added
                else parts.addLast(part)
            }

            return PosixPurePath(parts)
        }
    }

    // override impls
    override val rawComponents: List<ByteString> = rawParts
    override val components: List<String> by lazy {
        // impl the same as the interface, but is lazy-loaded rather than a getter
        rawComponents.map { it.decode() }
    }

    override val isAbsolute: Boolean get() = rawComponents[0] == SLASH
    override val rawName: ByteString by lazy {
        rawComponents.last()
    }

    override val name: String by lazy { rawName.decode() }

    override val parent: PosixPurePath by lazy {
        if (rawComponents.size >= 2) {
            // path a: multiple components
            // simple enough, return the second to last one
            PosixPurePath(rawParts = rawComponents.dropLast(1))
        } else {
            // path b: one component
            // absolute paths always resolve to / as the root path
            if (isAbsolute) SLASH_PATH
            // whereas relative paths resolve to the current directory
            else DOT_PATH
        }
    }

    override fun resolveChild(other: PurePath): PosixPurePath {
        require(other is PosixPurePath) { "Can only accept other Posix paths!" }

        // other absolute paths always win out
        // this is similar to pathlib.PurePath
        return if (other.isAbsolute) other
        // otherwise we just concatenate the other path components
        else {
            PosixPurePath(rawComponents + other.rawComponents)
        }
    }

    override fun withName(name: ByteString): PosixPurePath {
        require(!name.contains('/'.toByte())) { "Invalid name: $name" }

        val components = rawComponents.toMutableList()
        components[components.size - 1] = name
        return PosixPurePath(components)
    }

    override fun isChildOf(other: PurePath): Boolean {
        // un-absolute pure paths cannot be compared child-wise
        // as there exists no reference point to compare
        // but absolute paths can be compared with other absolutes
        // and relatives can be compared with other relatives
        if (isAbsolute != other.isAbsolute) {
            return false
        }

        // obviously false if the other one is bigger than us
        // also, if the path has the same amount of components it cannot be a child as at best it
        // will be the same directory
        if (other.rawComponents.size >= rawComponents.size) return false

        // check all the components of the other one, if it doesn't match us we're not a child
        // of it
        for ((c1, c2) in other.rawComponents.zip(rawComponents)) {
            if (c1 != c2) return false
        }

        // all matched in other, we are a child
        return true
    }

    override fun reparent(from: PurePath, to: PurePath): PosixPurePath {
        require(this.isChildOf(from)) { "$this is not a child of $from" }
        require(!this.isChildOf(to)) { "$this is already a child of $to" }

        // reallocate components to the correct size
        val size = (this.rawComponents.size - from.rawComponents.size) + to.rawComponents.size
        val components = ArrayList<ByteString>(size)

        // copy in the parts from the new parent
        components.addAll(to.rawComponents)
        // then copy in the parts from ourselves
        // starting at the offset of the first element past our current parent
        for (idx in from.rawComponents.size until this.rawComponents.size) {
            components.add(this.rawComponents[idx])
        }

        return PosixPurePath(components)
    }

    @Unsafe
    override fun unsafeToString(): String {
        val joined = if (isAbsolute) {
            val copy = rawComponents.toMutableList()
            copy[0] = b("")
            copy.join(SLASH)
        } else {
            rawComponents.join(SLASH)
        }

        return joined.unwrap().decodeToString(throwOnInvalidSequence = true)
    }

    // TODO: Maybe revist this implementation?
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is PurePath) return false
        return rawComponents == other.rawComponents
    }

    override fun hashCode(): Int {
        return rawComponents.hashCode()
    }

    override fun toString(): String {
        val s = rawComponents.joinToString(", ") { it.toString() }
        return "PosixPurePath[$s]"
    }
}
