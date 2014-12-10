
rem Determining JAVA Path
if not "%JAVA_HOME%" == "" goto gotJavaHome
 
   rem JAVA_HOME not set, searching for java.exe
   set JAVA_CMD=%~$PATH:1
   goto complete
:gotJavaHome
set JAVA_CMD=%JAVA_HOME%\bin\%1
:complete
