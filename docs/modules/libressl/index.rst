.. _module_libressl:

Tinlok TLS (LibreSSL)
=====================

This module contains an implementation of TLS sockets using the LibreSSL library.

.. warning::

    This is a dynamically linked library, and requires either LibreSSL or
    OpenSSL + LibreTLS (libtls ported to OpenSSL 3.0) in your linker path.

Installation
------------

Add the ``tinlok-tls-libtls`` module to your ``commonMain`` sourceset:

.. code-block:: kotlin

    configure<KotlinMultiplatformExtension> {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation(kotlin("stdlib"))
                    api("tf.lotte.tinlok:tinlok-tls-libtls:VERSION")
                }
            }
        }
    }

Add the specific sub-platforms to your other sourcesets:

.. code-block:: kotlin

    val linuxX64Main by getting {
        dependencies {
            api("tf.lotte.tinlok:tinlok-libtls-linuxx64:VERSION")
        }
    }

Contents
--------

This module provides an implementation of TLS sockets that implement the core TLS API.

* ``TlsClientSocket.unsafeOpen`` corresponds to ``TcpClientSocket.unsafeOpen`` over TLS.
* ``TlsClientSocket.open`` corresponds to ``TcpClientSocket.open`` over TLS.

These functions take an address (which will be used for server hostname verification), and an
optional ``TlsConfig``.

