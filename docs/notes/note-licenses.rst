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
are statically included into the relevant klibs for maximum ease of use.

* `ipv6-parser <https://github.com/jrepp/ipv6-parse>`_, licenced under the MIT licence. Used for
   handling IPv6 addresses.

* `Monocypher <https://monocypher.org/>`_, licenced under the 2-clause BSD licence. Used for core
   cryptography operations.

* `libtls <https://www.libressl.org/>`_, licenced under the ISC licence. Used for core TLS support.

  - ``libtls`` in its present form contains code from the OpenSSL project, which is
    under the OpenSSL/SSLeay licence which is normally incompatible with the LGPL. However, under
    the GPL Section 7 (Additional Terms), we give permission for any code in Tinlok to be linked
    with OpenSSL code. It is recommended that you do the same.

  - (Non-legal:) This clause is intended to be permanent; even if we switch away from using LibreSSL
    in the future the extra permission should be granted for all future versions, simply because
    OpenSSL (in forms pre-relicence such as in LibreSSL) is a useful library.


.. _GNU Lesser General Public License version 3.0 or later: https://www.gnu.org/licenses/lgpl-3.0.en.html
.. _Mozilla Public License Version 2.0 or later: https://www.mozilla.org/en-US/MPL/2.0/
