/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

package tf.lotte.tinlok.crypto

/**
 * Provides cryptography operations using the libsodium library.
 *
 * Creating a fresh instance of this object is safe; it does not allocate anything. This is the
 * default provider.
 */
public expect class LibsodiumProvider() : CryptographyProvider
