.. _issues:

Known Issues
============

Here's a list of issues I know about with the codebase:

* IP addresses are very basic

  - See Python's `ipaddress`_ module for what they would ideally be like.

* Several path functions do explicit checks instead of trying and failing.

  - This is both slower, and (potentially) riskier.

* No Windows-style paths yet

* Non-unicode paths don't work

  - This is a Kotlin issue; see `KT-41853`_.

* No pure-Kotlin path resolving

  - Basically means that every ``toAbsolutePath()`` has to call ``realpath(3)`` underneath

* No UDP socket support

* No TCP socket options (low hanging fruit)

* Not really optimised for performance

  - Allocations have tried to be kept to a minimum, but this isn't always achievable unfortunately

  - ByteString's are copied in several places, e.g. if reading is too small

  - Some methods like ``ByteString.split`` are particularly ripe for improvement

* Low-level system interface doesn't support non-blocking sockets

* If you are stupid and do fork-exec file descriptors will be copied (no O_CLOEXC yet)

.. _ipaddress: https://docs.python.org/3/library/ipaddress.html
.. _KT-41853: https://youtrack.jetbrains.com/issue/KT-41853
