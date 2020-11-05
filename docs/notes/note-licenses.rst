.. _licences:

Licences
========

Tinlok is dually licenced under the
`GNU Lesser General Public License version 3.0 or later`_, and the
`Mozilla Public License Version 2.0 or later`_.

What this practically means:

* No matter what, if you modify the source code of this library and redistribute it, you must
  provide the modified source code.

* If your library or application uses Tinlok statically linked (i.e. as a Kotlin application, or
  statically linked into your own library), if the LGPL is chosen your code will also be subject
  to the LGPL. If the MPL is chosen no such action applies.

This is primarily a concession to people who won't use copyleft licenses. If Kotlin ever gets
dynamic linking for Kotlin applications, rather than having the klib directly included, the MPL
will be removed and Tinlok will become purely LGPL.

External Projects
-----------------

Tinlok includes some statically linked external libraries to perform certain functionality. These
are statically included into the relevant klibs for maximum ease of use, and are distributed
under their original licences on Maven for anyone to use.

* `Monocypher <https://monocypher.org/>`_, licenced under the 2-clause BSD licence. Used for core
  cryptography operations.

The Tinlok source code also directly includes the header files of several external dependencies in
c-interop code.

* `libuuid <https://git.kernel.org/pub/scm/utils/util-linux/util-linux.git>`_, licenced under the
  3-clause (New) BSD licence.

* A copy of a structure used for symbolic links, taken from the MinGW source code (which was
  itself taken from the ReactOS source code). Licenced under the public domain.


.. _GNU Lesser General Public License version 3.0 or later: https://www.gnu.org/licenses/lgpl-3.0.en.html
.. _Mozilla Public License Version 2.0 or later: https://www.mozilla.org/en-US/MPL/2.0/

