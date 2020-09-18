.. _issues:

Known Issues
============

Here's a list of issues I know about with the codebase:

* Non-unicode paths don't work

  - This is a Kotlin issue; see `KT-41853<https://youtrack.jetbrains.com/issue/KT-41853>`_.

* No pure-Kotlin path resolving

  - Basically means that every ``toAbsolutePath()`` has to call ``realpath(3)`` underneath

* No UDP socket support

* No connect() timeout support

* Not really optimised for performance

  - Allocations have tried to be kept to a minimum, but this isn't always achievable unfortunately

  - Some methods like ``ByteString.split`` are particularly ripe for improvement

* Low-level system interface doesn't support non-blocking sockets

* Path extensions really really like to call lstat()

* If you are stupid and call fork() (DON'T CALL FORK) file descriptors will be copied (no
  O_CLOEXC yet)

