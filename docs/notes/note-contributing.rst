.. _contributing:

Contributing
============

Tinlok uses Mercurial for version control, as Mercurial is superior to Git. Unfortunately, Git
won despite being worse and as such there are very few good public places to host Mercurial
repositories.

Currently, Tinlok uses a GitHub_ repository as its primary repository, using hg-git. At some
point, I aim to get Tinlok moved to either the public Heptapod_ instance, a self-hosted instance,
or some other self-hosted Mercurial webserver (in reverse preference order).

Contributions will always be accepted via the GitHub repo, but issues will be disabled. Pull
requests will be merged manually via hg instead of via the web UI.

Style
-----

* Keep all platform calls in the platform-specific ``Syscall`` object.

* Any API that can have any hint of memory corruption, resource leaks, or other general unsafety
  should be annotated with ``@Unsafe``.

* Use IntelliJ's reformatter to format your Kotlin code.

  - Don't use IntelliJ? Sucks to be you.

* Keep as much code in the common platform module as possible, only using native code for actual
  native calls.

* Use ``try/finally``, ``memScoped``, and inline functions passed a lambda whenever possible for
  resource management.


.. _GitHub: https://github.com/Fuyukai/Tinlok
.. _Heptapod: https://foss.heptapod.net/
