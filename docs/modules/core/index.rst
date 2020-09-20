.. _module_core:

Tinlok Core
===========

The core module is the primary module for Tinlok. The core module contains:

* Path helpers (inspired by Python's ``pathlib``)
* Filesystem I/O
* Network I/O
* (Planned) Subprocessing
* (Planned) Full time/date library
* (Planned) Hashing
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
                    api("tf.lotte.tinlok:tinlok-core:1.0.0")
                }
            }
        }
    }

Contents
--------

.. toctree::
   :maxdepth: 1

   bytestring.rst
   io-interfaces.rst
   paths/pure.rst
   paths/io.rst
