.. _core-tls:

Networking - TLS (Core)
=======================

Tinlok Core provides the API for TLS sockets so that libraries can be TLS-aware if needed without
specifically linking to a TLS backend library.

* The ``TlsConfig`` data class is used to configure a TLS connection. It provides these attributes:

  - ``useTlsV12`` - If TLS 1.2 should be used alongside TLS 1.3.
  - ``alpnProtocols`` - A list of ALPN protocols to send (e.g. ``h2``).

.. note::

    The configuration object does not let you change things like cipher suites. I see zero
    practical reason for not using the defaults that isn't "giving yourself a security vuln". If
    you have a legitimate usecase, let me know.

* The ``TlsClientSocket`` is an interface inheriting ``TcpClientSocket`` which is used for all
  TLS client connection objects.

* The ``TlsException`` is an exception class inheriting from ``OSException`` that is thrown on
  TLS errors. Its errno is always ``-1``.

More to come; stay tuned.
