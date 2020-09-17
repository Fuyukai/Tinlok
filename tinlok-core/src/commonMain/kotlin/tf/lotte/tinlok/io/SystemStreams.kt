/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.io

/*
/**
 * A [ReadableStream] corresponding to standard input.
 */
public expect var STDIN: ReadableStream

/**
 * A [ReadableStream] corresponding to standard output.
 */
public expect var STDOUT: WriteableStream

/**
 * A [WriteableStream] corresponding to standard error.
 */
public expect var STDERR: WriteableStream


/**
 * An enhanced version of [println] which writes to [STDOUT] (either the system stream or
 * whatever file is put there), and joins all of the provided items using [sep].
 */
public fun puts(
    vararg items: Any?, sep: String = " ", end: String = "\n", file: Writeable = STDOUT
) {
    val joined = items.joinToString(sep) + end
    file.writeAll(joined.toByteString())
}
*/
