/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.net.socket

/**
 * An enumeration of possible socket shutdown options.
 */
public enum class ShutdownOption(public val number: Int) {
    READ(0),
    WRITE(1),
    READWRITE(2)
}
