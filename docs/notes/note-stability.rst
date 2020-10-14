A note on stability
===================

Tinlok provides zero stability guarantees. Public APIs can, and will, change between minor or
very minor releases.

At some point in the future, a ``Stable`` annotation may be introduced to designate APIs that are
sufficiently stable that they will get a deprecation cycle instead of being ripped out.

Tinlok versioning
-----------------

Tinlok follows my own versioning scheme which prioritises API soundness over the arbitrary notion
of API stability. One day, somebody could decide behaviour you depend on is actually a bug and
break it and it would be perfectly fine under most versioning schemes, and eventually everyone
ends up with Chrome versioning anyway, so I just skip all the way to breaking things.

Tinlok version numbers consist of three parts:

* Major version number

  - The first number. This increments when either a) no reasonable program will be compatible
    with the changes within or b) whenever I want to.

* Minor version number

  - The second number. This increments every feature release.

* Very minor version number

  - The third number. This increments to do bug fixes and other boring work.


This sounds terrible
--------------------

I don't care. Why are you using alpha software if you care about stability, anyway?
