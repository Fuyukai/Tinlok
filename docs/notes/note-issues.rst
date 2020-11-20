.. _issues:

Known Issues
============

Here's a list of issues I know about with the codebase:

* IP addresses are very basic

  - See Python's `ipaddress`_ module for what they would ideally be like.

* IPv6 parser doesn't support IPv4 addresses

* Several path functions do explicit checks instead of trying and failing.

  - This is both slower, and (potentially) riskier.

* No pure-Kotlin path resolving

  - Basically means that every ``toAbsolutePath()`` has to call ``realpath(3)`` underneath

* No buffer operations for datagrams

* Not really optimised for performance

  - Allocations have tried to be kept to a minimum, but this isn't always achievable unfortunately

  - ByteString's are copied in several places, e.g. if reading is too small

  - Some methods like ``ByteString.split`` are particularly ripe for improvement

* If you are stupid and do fork-exec file descriptors will be copied (no O_CLOEXC yet)

* Win32 support:

  * Win32 path methods will do a LOT of system calls

    - Thank its weird concept of symbolic links for this.

    - Linux doesn't have this issue because symlinks can always be treated as just files, and it
      has realpath() to do the symlink shenanigans for us.

    - I think some can be reduced, at least, by merging some checks...

  * No overlapped support on files. (Yet!)

  * File handles require a new allocation due to an ``expect/actual`` bug.

* Several interfaces are missing convenient default values.

  - This is ALSO a Kotlin bug!

.. _ipaddress: https://docs.python.org/3/library/ipaddress.html
.. _KT-41853: https://youtrack.jetbrains.com/issue/KT-41853
