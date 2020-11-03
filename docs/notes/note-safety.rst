.. _safety:

A note on safety
================

Tinlok has two concepts of safety: system call safety, and API safety.

System call safety
------------------

Tinlok is built upon calls to ``libc`` (on POSIX platforms) or the Windows API (on Windows
platforms). These calls are easy to misuse, can corrupt memory, and can generally ruin your day,
so KNSTE hides them behind platform-specific objects with all functions marked as ``Unsafe``.
There is no common abstraction for these unsafe calls; only the safe wrappers around them.

API Safety
----------

Some common APIs are marked as unsafe, for varying reasons:

* Easy to misuse

  - Some common APIs require extra setup before they can be used properly, or can corrupt memory,
    or other similarly evil things.

* Resource leaks

  - Kotlin/Native has no concept of finalisers. This means that opening a file without making
    sure you close it later on will leak that file descriptor, permanently.

The ``Unsafe`` annotation
-------------------------

The safety barrier is enforced by Kotlin's ``OptIn`` system, and the ``Unsafe`` annotation.
**All** consumers of unsafe APIs must mark their caller as ``@Unsafe`` or ``@OptIn(Unsafe::class)``
to use unsafe APIs. Many of Tinlok's APIs guard unsafe APIs in safe functions so it should be
very rare to ever need to use ``@Unsafe`` functions directly.

The ``Unsafe`` annotation is provided by the :ref:`module_copperchain` module so it can be used
outside of Tinlok-related code.
