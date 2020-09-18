.. _fork:

A note on fork
==============

Do not call fork(2). If you have a situation where it is appropriate to call fork, consider
asking for a new API in the library or using the standard subprocessing (WIP) API.
Do not be clever and call clone/clone3, either.

Tinlok does not (and will not) use SIGCHLD for child management; subprocesses will be implemented
around pidfd's instead, and as such your child processes will never be reaped if you don't call
waitpid yourself. (At which point, why are you even forking? Just run your code.)

In the future, Tinlok may spawn a finaliser thread to prevent resource leaks; this will probably
break with forks too.
