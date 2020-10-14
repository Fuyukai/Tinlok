.. _cryptography-core:

Cryptography
============

Tinlok provides several lightweight and fast core cryptographic functions using modern algorithms.
These are powered by the Monocypher library.

Integrity hashes
----------------

The ``Blake2b`` class is provided for performing integrity hashes. Whilst Blake2b normally has
variable length hashes, this class only produces 512-bit (64 byte) hashes. Obviously, this uses
the `Blake2b`_ algorithm.

.. code-block:: kotlin

    val toHash = b("The quick brown fox jumps over the lazy dof")
    // no key
    val hash = Blake2b { hasher ->
        hasher.feed(toHash)
        hasher.hash()
    }
    println(hash.hexdigest())
    // securely compare the two hashes
    val hash2 = toHash.blake2b()
    assert(hash.secureCompare(hash2))

Password hashes
---------------

The ``passwordHash`` function is provided for creating password hashes. This function uses the
`Argon2i`_ algorithm, which is a modern and secure password hasher/key derivation algorithm.

The default parameters are tuned for a reasonable level of security. It is recommended to adjust
the memory limit for the maximum amount of memory available.

.. code-block:: kotlin

    val secret = "password"  // not very secure!
    val hash: Argon2iHash = passwordHash(secret)
    // re-derives the hash, then securely compares them
    assert(hash.verify(secret))

Secure Random Numbers
---------------------

Tinlok provides a ``SecureRandom`` object that provides securely generated random numbers. This
is a ``kotlin.random.Random`` and thus can be used anywhere that a regular ``Random`` is.


.. _Blake2b: https://en.wikipedia.org/wiki/BLAKE_(hash_function)#BLAKE2
.. _Argon2i: https://en.wikipedia.org/wiki/Argon2
