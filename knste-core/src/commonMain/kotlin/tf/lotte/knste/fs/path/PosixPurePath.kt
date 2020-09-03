/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of KNSTE.
 *
 * KNSTE is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.knste.fs.path

import tf.lotte.knste.*

/**
 * A pure path that uses POSIX semantics. You probably don't want to use this class directly.
 */
public class PosixPurePath(rawParts: List<ByteString>) : PurePath {
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

    override val isAbsolute: Boolean = rawComponents[0] == SLASH
    override val rawName: ByteString
        get() = rawComponents.last()

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

    override fun join(other: PurePath): PosixPurePath {
        if (other !is PosixPurePath) error("Can only accept other Posix paths!")

        // other absolute paths always win out
        // this is similar to pathlib.PurePath
        return if (other.isAbsolute) other
        // otherwise we just concatenate the other path components
        else {
            PosixPurePath(rawComponents + other.rawComponents)
        }
    }

    override fun join(other: ByteString): PosixPurePath {
        if (other.isEmpty()) return this

        val newComponents = rawComponents.toMutableList()
        newComponents.add(other)
        val bs = newComponents.join(SLASH)
        // force a re-parse of the overall path
        return fromByteString(bs)
    }

    override fun extremelyUnsafeToKotlinStringPleaseYellAtLangDevIfThisFails(): String {
        val joined = rawComponents.join(SLASH)
        return joined.toByteArray().decodeToString(throwOnInvalidSequence = true)
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
        val s = rawComponents.map { it.toString() }.joinToString(", ")
        return "PosixPurePath[$s]"
    }
}
