.. _module_copperchain:

Copperchain
===========

Copperchain is a common module depended on by Tinlok that provides various interface definitions
or utility classes/functions that are not specific to a native environment.

Copperchain is built for all available Kotlin platforms.

Installation
------------

Add the ``copperchain`` module to your ``commonMain`` sourceset:

.. code-block:: kotlin

    configure<KotlinMultiplatformExtension> {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation(kotlin("stdlib"))
                    api("tf.lotte.copperchain:copperchain:VERSION")
                }
            }
        }
    }

Add the specific sub-platforms to your other sourcesets:

.. code-block:: kotlin

    val linuxX64Main by getting {
        dependencies {
            api("tf.lotte.copperchain:copperchain-linuxx64:VERSION")
        }
    }

Contents
--------

.. toctree::
   :maxdepth: 2

   closingscope.rst
   bytestring.rst
   io-interfaces.rst
