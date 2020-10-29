/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.fs.path

import tf.lotte.cc.Unsafe
import tf.lotte.cc.types.*

/**
 * Implements a pure path with Windows filesystem semantics.
 */
public open class WindowsPurePath protected constructor(
    internal val driveLetter: ByteString?,
    internal val volume: ByteString?,
    internal val rest: List<ByteString>
) : PurePath {
    public companion object {
        protected const val SEP: Byte = '\\'.toByte()
        protected const val ALTSEP: Byte = '/'.toByte()
        protected val DRIVE_SEP: ByteString = b(":\\")
        protected val UNC_SEP: ByteString = b("\\\\")
        protected val LONGPATH: ByteString = b("\\\\?\\")
        protected val ILLEGAL: Set<Byte> = listOf(
            '<', '>', ':', '"', '|', '?', '*', '\\', '/'
        ).mapTo(mutableSetOf()) { it.toByte() }
        protected val DOT: ByteString = b(".")

        @OptIn(ExperimentalStdlibApi::class)
        protected val ILLEGAL_NAMES: Set<ByteString> = buildList<String> {
            add("CON")
            add("PRN")
            add("AUX")
            add("NUL")
            for (i in 0..9) {
                add("COM$i")
                add("LPT$i")
            }
        }.mapTo(mutableSetOf()) { it.toByteString() }

        /**
         * Checks if a name is illegal.
         */
        public fun checkIllegalName(component: ByteString): Boolean {
            if (component in ILLEGAL_NAMES) return true
            for (char in component) {
                if (char in ILLEGAL) return true
            }

            return false
        }

        /**
         * Splits a path up using the sep and altsep.
         */
        @OptIn(Unsafe::class)
        protected fun splitPath(path: ByteString): List<ByteString> {
            // sizes pre-allocated for worst case scenarios
            val items = ArrayList<ByteString>(path.size)
            val working = ByteArray(path.size)
            var cursor = 0

            for (byte in path) {
                if (byte == SEP || byte == ALTSEP) {
                    // don't copy empty segments
                    if (cursor == 0) continue
                    // reached separator, copy working into items
                    val copy = working.copyOfRange(0, cursor)
                    val wrapped = ByteString.fromUncopied(copy)

                    // ensure its a legal name
                    if (checkIllegalName(wrapped)) {
                        throw IllegalArgumentException("Component $wrapped is illegal")
                    }

                    // don't add single . names
                    if (wrapped != DOT) {
                        items.add(ByteString.fromUncopied(copy))
                    }
                    cursor = 0
                } else {
                    // not separator, add to working space and continue
                    working[cursor] = byte
                    cursor += 1
                }
            }

            // copy any leftovers
            if (cursor > 0) {
                val copy = working.copyOfRange(0, cursor)
                val bs = ByteString.fromUncopied(copy)

                // always ensure its a legal name
                if (checkIllegalName(bs)) {
                    throw IllegalArgumentException("$path contains illegal substring: $bs")
                }

                // don't add single . names
                if (bs != DOT) {
                    items.add(ByteString.fromUncopied(copy))
                }
            }

            return items
        }

        /**
         * Finds the end of the UNC anchor.
         */
        protected fun findUncAnchor(path: ByteString): Int {
            // this finds the SECOND \
            // e.g. when scanning \\device\share it will attempt to skip past the initial \\,
            // find the FIRST \, then find the next \ after that.
            var cursor = 2
            var foundFirst = false
            while (true) {
                // EOF, just assume this is entirely anchor
                if (cursor >= path.size) {
                    return path.size
                }

                val item = path[cursor]
                if (item == SEP || item == ALTSEP) {
                    if (foundFirst) return cursor
                    else foundFirst = true
                }

                cursor += 1
            }
        }

        /**
         * Parses a path into a Triple of (DriveLetter, VolumeName, Parts).
         */
        @OptIn(Unsafe::class)
        protected fun parsePath(
            bs: ByteString
        ): Triple<ByteString?, ByteString?, List<ByteString>> {
            // strip \\?\ returned by various W functions
            val realPath = if (bs.startsWith(LONGPATH)) {
                bs.substring(4, bs.size)
            } else {
                bs
            }

            if (realPath.substring(1, 3) == DRIVE_SEP) {
                // path one: absolute path, with drive letter
                val anchor = ByteString.fromUncopied(byteArrayOf(realPath[0], realPath[1]))
                // some weird paths may use the C:\\ form, even though that hasn't been needed in
                // years

                val sub = if (realPath.size >= 4 && realPath[3] == '\\'.toByte()) {
                    realPath.substring(4, realPath.size)
                } else realPath.substring(3, realPath.size)

                return Triple(anchor, null, splitPath(sub))
            } else if (realPath.startsWith(UNC_SEP)) {
                // path two: absolute path, with UNC
                val anchorIdx = findUncAnchor(realPath) + 1
                val anchor = realPath.substring(0, anchorIdx)
                val rest = realPath.substring(anchorIdx, realPath.size)
                return Triple(null, anchor, splitPath(rest))
            } else if (realPath.startsWith(SEP) || realPath.startsWith(ALTSEP)) {
                throw UnsupportedOperationException("Drive-letter relative paths are unsupported")
            } else {
                // path four: relatives
                val split = splitPath(realPath)
                return Triple(null, null, split)
            }
        }

        /**
         * Creates a new [WindowsPurePath] from the specified ByteString.
         */
        @OptIn(Unsafe::class)
        public fun fromByteString(bs: ByteString): WindowsPurePath {
            val parsed = parsePath(bs)
            return WindowsPurePath(parsed.first, parsed.second, parsed.third)
        }

        /**
         * Creates a new [WindowsPurePath] from the specified String.
         */
        public fun fromString(s: String): WindowsPurePath {
            return fromByteString(s.toByteString())
        }
    }

    override val rawAnchor: ByteString?
        get() = driveLetter ?: volume

    override val isAbsolute: Boolean
        get() = rawAnchor != null

    override val rawName: ByteString? get() {
        return if (isAbsolute) {
            // first part of component may be the anchorr
            if (rawComponents.size <= 1) null
            else rawComponents.last()
        } else {
            // always exists on relative paths
            rawComponents.last()
        }
    }

    override val parent: WindowsPurePath by lazy {
        if (rest.isEmpty()) this
        else {
            // .lastIndex achieves this but its unclear as to wtf it does
            val newRest = rest.subList(0, rest.size - 1)
            WindowsPurePath(driveLetter, volume, newRest)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override val rawComponents: List<ByteString> = buildList<ByteString> {
        rawAnchor?.let { add(it) }
        addAll(rest)
    }

    override val anchor: String? by lazy { rawAnchor?.decode() }
    override val name: String? by lazy { rawName?.decode() }
    override val components: List<String> by lazy { rawComponents.map { it.decode() } }

    @Unsafe
    override fun unsafeToString(): String {
        return components.joinToString("\\")
    }

    override fun isChildOf(other: PurePath): Boolean {
        if (other !is WindowsPurePath) return false

        // not always true, anchors can be different and equal (e.g. UNC drive letters)
        if (isAbsolute) {
            if (!other.isAbsolute) return false
            if (anchor != other.anchor) return false
        }

        // copied from posixpurepath

        // obviously false if the other one is bigger than us
        // also, if the path has the same amount of components it cannot be a child as at best it
        // will be the same directory
        if (other.rawComponents.size >= rawComponents.size) return false

        // check all the components of the other one, if it doesn't match us we're not a child
        // of it
        for ((c1, c2) in other.rawComponents.zip(rawComponents)) {
            if (c1 != c2) return false
        }

        return true
    }

    override fun withName(name: ByteString): WindowsPurePath {
        require(!checkIllegalName(name)) { "$name is an illegal path" }

        return if (rawName == null) {
            // empty name, can't edit empty contents
            val components = mutableListOf(name)
            WindowsPurePath(driveLetter, volume, components)
        } else {
            // can edit contents here tho
            val components = rest.toMutableList()
            components[components.lastIndex] = name
            WindowsPurePath(driveLetter, volume, components)
        }
    }

    override fun resolveChild(other: PurePath): WindowsPurePath {
        require(other is WindowsPurePath) { "Can only accept other Windows paths!" }

        // same as posix paths, the second absolute path always wins
        return if (other.isAbsolute) other
        else {
            WindowsPurePath(driveLetter, volume, rest + other.rest)
        }
    }

    override fun reparent(from: PurePath, to: PurePath): WindowsPurePath {
        require(from is WindowsPurePath) { "Can only accept other Windows paths!" }
        require(to is WindowsPurePath) { "Can only accept other Windows paths!" }
        require(this.isChildOf(from)) { "$this is not a child of $from" }
        require(!this.isChildOf(to)) { "$this is already a child of $to" }

        // still same as posix paths
        val size = (this.rest.size - from.rest.size) + to.rest.size
        val components = ArrayList<ByteString>(size)

        components.addAll(to.rest)
        for (idx in from.rest.size until this.rest.size) {
            components.add(this.rest[idx])
        }

        return WindowsPurePath(to.driveLetter, to.volume, components)
    }

    override fun toString(): String {
        val s = rawComponents.joinToString(", ") { it.toString() }
        return "WindowsPurePath[$s]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is WindowsPurePath) return false

        if (anchor != other.anchor) return false
        if (rest != other.rest) return false

        return true
    }

    override fun hashCode(): Int {
        var result = anchor?.hashCode() ?: 0
        result = 31 * result + rest.hashCode()
        return result
    }

}
