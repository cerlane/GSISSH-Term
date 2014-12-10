rem @echo off
rem  Lookup the location of java.exe
call findjava.bat javaw.exe
if not "%JAVA_CMD%" == "" goto gotJavaCmd
   echo You must set JAVA_HOME to point at your Java Development Kit installation
   echo or configure your PATH to include java.exe
   goto cleanup
:gotJavaCmd
if not "%SSHTOOLS_HOME%" == "" goto gotHome
set SSHTOOLS_HOME=..
:gotHome
set _LIBJARS=
for %%i in (%SSHTOOLS_HOME%\lib\*.jar) do call %SSHTOOLS_HOME%\bin\cpappend.bat %%i
set _LIBJARS=%_LIBJARS%;
for %%i in (%SSHTOOLS_HOME%\lib\jglobus2\*.jar) do call %SSHTOOLS_HOME%\bin\cpappend.bat %%i

if not "%_LIBJARS%" == "" goto runapp
echo Unable to set CLASSPATH dynamically.
goto cleanUp
:runapp
call "%JAVA_CMD%" -classpath %_LIBJARS% -Dsshtools.home=%SSHTOOLS_HOME% -Dlog4j.properties=sshterm.properties com.sshtools.sshterm.SshTerm %*
:cleanUp
set _LIBJARS=
set JAVACMD=
set SSHTOOLS_HOME=
