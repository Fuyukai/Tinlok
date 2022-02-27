/*
 * Copyright (C) 2020-2022 Lura Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.veriny.tinlok.util

/**
 * Sleeps for the specified number of microseconds.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect fun microsleep(micros: UInt)
