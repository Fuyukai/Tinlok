/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.fs

/**
 * Enumeration of standard open modees.
 */
public enum class StandardOpenModes : FileOpenMode {
    /** Opens the file for reading. */
    READ,

    /** Opens the file for writing, removing its existing content. */
    WRITE,

    /** Opens the file for writing, appending to the existing content. */
    APPEND,

    /** Creates a new file, ignoring if it already exists. */
    CREATE,

    /** Creates a new file, throwing an error if it already exists. */
    CREATE_NEW,


    ;
}
