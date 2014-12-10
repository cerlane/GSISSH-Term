
'gssapi-keyex' user authentication method (contributed by Jon Siwek).
implemented by DM (18/11/09)
=======================================================================
Removes 'external-keyx' userauth method (from draft RFC 4462) and implements
'gssapi-keyex' as per the final RFC 4462 GSS-API Authentication and Key
Exchange for the SSH Protocol (http://tools.ietf.org/html/rfc4462).

# Renamed & modified: EKEYXAuthenticationClient.java -> GSSAPIKeyexAuthenticationClient.java
Added             : GSSAPIKeyexAuthenticationClient.java
Modified          : SshToolsApplicationClientPanel.java (to reflect new name of class)
                    SshAuthenticationClientFactory.java




Notes taken from: http://dev.globus.org/wiki/GSI-OpenSSH/Internals

    * RFC 4462 specifies the use of GSSAPI for both server authentication
      (in the SSH transport layer protocol) and client authentication (in the SSH authentication or "ssh-userauth" protocol).
    * GSSAPI server authentication replaces the need for SSH host keys and "known_hosts" files.
    * OpenSSH currently implements GSSAPI client authentication only.
    * OpenSSH currently supports only the Kerberos GSSAPI mechanism.
    * RFC 4462 specifies the following user authentication methods:
          - "gssapi-keyex": This method is used when key exchange and server authentication
            (in the SSH transport layer protocol) used GSSAPI and the GSSAPI context
            includes the client's (user's) identity (i.e., GSSAPI mutual authentication
            was performed). If the client identity is accepted and authorized, this
            method succeeds without needing to perform a second GSSAPI exchange.
            (OpenSSH does not implement this method.)
          - "gssapi-with-mic": This method performs the GSSAPI exchange to authenticate the client. (OpenSSH implements this method.)

    * Draft versions of RFC 4462 (draft-ietf-secsh-gsskeyex-06 and earlier) specified
      the following user authentication methods:
          - "external-keyx": The precursor to "gssapi-keyex". The difference is that
            "gssapi-keyex" includes a MIC (message integrity check) whereas "external-keyx" doesn't.
          - "gssapi": The precursor to "gssapi-with-mic". Again, the difference is
             that "gssapi-with-mic" includes a MIC (message integrity check) whereas "gssapi" doesn't.