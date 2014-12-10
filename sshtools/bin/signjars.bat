rem @echo off
for %%i in (*.jar) do %JAVA_HOME%\bin\jarsigner -keystore %1 -storepass %3 %%i %2
