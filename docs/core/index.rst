.. _module_core:

Tinlok Core
===========

The core module is the primary module for Tinlok. The core module contains:

* Path helpers (inspired by Python's ``pathlib``)
* Filesystem I/O
* Network I/O
* High-level cryptography
* (Planned) Subprocessing
* (Planned) Compression/decompression (zlib, gzip, lzma)
* (Planned) Maybe more?

Installation
------------

Add the ``tinlok-core`` module to your ``commonMain`` sourceset:

.. code-block:: kotlin

    configure<KotlinMultiplatformExtension> {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation(kotlin("stdlib"))
                    api("tf.lotte.tinlok:tinlok-core:VERSION")
                }
            }
        }
    }

Add the specific sub-platforms to your other sourcesets:

.. code-block:: kotlin

    val linuxX64Main by getting {
        dependencies {
            api("tf.lotte.tinlok:tinlok-core-linuxx64:VERSION")
        }
    }

Contents
--------

.. toctree::
   :maxdepth: 2

   closingscope.rst
   bytestring.rst
   cryptography.rst
   uuid.rst

   buffer.rst
   streams-listeners.rst

   paths/pure.rst
   paths/io.rst

   networking/ipaddress.rst
   networking/network-enums.rst
   networking/socketaddress.rst
   networking/sockets.rst
