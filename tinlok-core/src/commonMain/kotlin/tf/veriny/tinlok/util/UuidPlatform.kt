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
 * Generates a Version 1 (MAC + Time) UUID.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect fun uuidGenerateV1(): UByteArray

/**
 * Generates a Version 4 (psuedorandom) UUID.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public expect fun uuidGenerateV4(): UByteArray
