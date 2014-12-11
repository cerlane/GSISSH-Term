This is a LRZ (www.lrz.de) customised version of the original GSI-SSHTerm maintained by NCS.

Major  features supported in this customised version:
-----------------------------------------------------

1) Automatic installation and update of CA certificates via a PRACE (www.prace-ri.eu) managed CA service. This includes all CAs from the IGTF bundle (including EUGridPMA and TAGPMA). To check if your CA is supoorted: http://winnetou.sara.nl/deisa/certs/docs/index.html

2) MyProxy Tool - Generate and upload both regular and VOMS-enabled proxy to a myproxy server.

3) Virtual Organisation support (both EGI (www.egi.eu) and user customised). All EGI supported VOs VOMS configuration are automatically installed and updated when using the tool. Enable the generation of a VOMS proxy

4) Globus cloud based GridFTP (file transfer) service UI. This service was originally known as Globus Online.


Recommendations
---------------

It is typically recommended for users to use the JAVAWS version when possible. You will get the latest update automatically.

Alternatively, you can download a precompiled version from http://www.lrz.de/services/compute/grid_en/software_en/gsisshterm_en/#setup-gsissh  and manually check for updates.

Building and compiling from source is only meant for advance users who intends to make code changes.


Known issues
------------

1)  Login using certificates in browsers (Mozilla Firfox and Internet Explorer) is broken. Login from Chrome or Safari on Mac OSX (certificates are in Keychain.accesss) still works.

2) VNC tool, based on tightVNC, is also broken due to a too old copy and thus deprecated protocol.



To build/install:
------------------------

cd sshtools

./make.sh all


To run:
-------

cd sshtools/bin

./sshterm.sh     

or 

./sshterm.bat    {Windows users}

For more information about his tool:
------------------------------------

http://www.lrz.de/services/compute/grid_en/software_en/gsisshterm_en/





