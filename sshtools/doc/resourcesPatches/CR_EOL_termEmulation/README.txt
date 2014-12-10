Use "CR" EOL settings for terminal emulation (contributed by Jon Siwek).
implemented by DM (18/11/09)
=============================================

The "Default" setting for end-of-line in the terminal emulation was guessed
at depending upon the SSH server version string, which is not a logical thing
to do. The patch is mostly documentation changes, the only code change is in
TransportProtocolCommon.java

SSH Transport Layer Protocol (RFC 4253) states that the SSH version
string MUST always end with (CR)(LF), except in the case that server
wants to operate in compatibility mode for SSH protocol 1.x.  For
that situation the server SHOULD NOT send the (CR).

"LF" (or "CR" as the Connection Settings Tab calls it) is appropriate
for any client since CR+LF in the terminal emuluation will cause:
   - Text editors will insert two newlines for every 1 press of return
   - Password prompts will catch the extra carriage return and
       think it is part of the password, thus failing
