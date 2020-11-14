/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:OptIn(ExperimentalUnsignedTypes::class)
package tf.lotte.tinlok.system

/**
 * Common bridge function for eventfd.
 */
internal expect fun __eventfd(count: UInt, flags: Int): Int
