.. _static-libraries:

External Static Libraries
=========================

Tinlok includes some statically linked external libraries to perform certain functionality. These
are statically included into the relevant klibs for maximum ease of use, and are distributed
under their original licences on Maven for anyone to use.

All external libraries are provided under the ``external.$LIB_NAME`` Kotlin package.

Monocypher
----------

`Monocypher <https://monocypher.org/>`_, licenced under the 2-clause BSD licence. Used for core
cryptography operations.

Monocypher bindings are available using the ``tinlok-static-monocypher`` library, like so:

.. code-block:: kotlin

    dependencies {
        implementation("tf.veriny.tinlok:tinlok-static-monocypher:TINLOK-VERSION")
    }


libuuid
-------

`libuuid <https://git.kernel.org/pub/scm/utils/util-linux/util-linux.git>`_, licenced under the
3-clause (New) BSD licence.

libuuid bindings are available using the ``tinlok-static-libuid`` library, like so:

.. code-block:: kotlin

    dependencies {
        implementation("tf.veriny.tinlok:tinlok-static-libuuid:TINLOK-VERSION")
    }

Various Other Sources
---------------------

Tinlok also uses some other source code definitions that aren't covered by these libraries:

* A copy of a structure used for symbolic links, taken from the MinGW source code (which was
  itself taken from the ReactOS source code). Licenced under the public domain.
