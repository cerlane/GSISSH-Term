
Contributed by Jon Siwek, implemented by DM (18/11/09)
======================================================
See included patch file: Xforward-mac-linuxamd64.patch

Allows X11 forwarding from Mac OS X and Linux 64-bit clients.
The native junixsocket code has been compiled for these architectures
(for Mac it is a universal binary) and included in modified
dependency jar: jlirc-unix-soc.jar.

The modifications to jlirc-unix-soc.jar also includes a modified
jlirc UnixSocketImpl.java as in the the included patch: jlirc-unixsocket.patch.
It extracts the shared libraries from the jar to a temporary location,
since they cannot be loaded directly from a jar.

The jlirc projec home: http://sourceforge.net/projects/jlirc/
Note, the jar that is included as part of gsisshterm (jlirc-unix-soc.jar)
only includes the "org/lirc/socket" package.
