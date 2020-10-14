.. _index:

Tinlok
======

Tinlok is a library providing the missing 75% of the Kotlin/Native Desktop standard library.

Tinlok focuses on **usability** and **correctness**, *not* simplicity. Computers are complicated
and we don't pretend otherwise, but Tinlok tries to make that complication as easy to use as
possible.

Why?
----

The Kotlin stdlib is very barebones. Kotlin is typically a guest language on somebody else's
virtual machine, so most Kotlin code is designed around integrating with that platform's core
functionality, sometimes with helper glue code to make it more natural to use in a Kotlin context.

Kotlin/Native on Desktop, however, is a host language and has no platform to work with aside from
the platform libraries (libc or Win32) and these libraries are not ergonomic to use from a Kotlin
context. Tinlok attempts to fill this gap by wrapping the platform libraries in helpful
Kotlin-esque high-level APIs.

Where?
------

In source form, Tinlok can currently be found on GitHub. See :ref:`contributing` for more
information.

In binary form, Tinlok can be found on Bintray:

.. code-block:: kotlin

    allprojects {
        repositories {
            maven { url = uri("https://dl.bintray.com/constellarise/tinlok") }
        }
    }

    subprojects {
        apply(plugin = "org.jetbrains.kotlin.multiplatform")

        configure<KotlinMultiplatformExtension> {
            sourceSets {
                val commonMain by getting {
                    api("tf.lotte.tinlok:MODULE:VERSION")
                }
            }
        }
    }

.. toctree::
   :maxdepth: 1
   :caption: Important info

   notes/note-safety.rst
   notes/note-platforms.rst
   notes/note-stability.rst
   notes/note-issues.rst
   notes/note-licenses.rst
   notes/note-contributing.rst
   notes/changelog.rst

.. toctree::
   :maxdepth: 3
   :caption: Modules

   modules/core/index.rst
   modules/libressl/index.rst



