rem @echo off
rem  Lookup the location of java.exe
call bin\findjava.bat java.exe
if not "%JAVA_CMD%" == "" goto gotJavaCmd
   echo You must set JAVA_HOME to point at your Java Development Kit installation
   echo or configure your PATH to include java.exe
   goto cleanup
:gotJavaCmd
set SSHTOOLS_HOME=.
:okHome
set ANT_HOME=.\dev\ant
set ANT_LIB=%ANT_HOME%\lib

set _LIBJARS=
for %%i in (%ANT_LIB%\*.jar) do call bin\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto runapp
echo Unable to set CLASSPATH dynamically.
goto cleanUp
:runapp
set _LIBJARS=%_LIBJARS%;%JAVA_HOME%\lib\tools.jar
call "%JAVA_CMD%" -classpath %_LIBJARS% org.apache.tools.ant.Main -buildfile build.xml %*
:cleanUp
set _LIBJARS=
set JAVACMD=
set SSHTOOLS_HOME=

