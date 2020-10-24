/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

// needed to generate the right empty file due to how typealiases work
@file:JvmName("__CloseableKt")
package tf.lotte.cc

// simple typealias for AutoCloseable, as it has the same semantics
public actual typealias Closeable = AutoCloseable