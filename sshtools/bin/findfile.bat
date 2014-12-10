rem Finding file
echo Searching PATH for %1
set FIND_FILE=%~$PATH:1
if not "%FIND_FILE%" == "" goto :complete
echo PATH=%PATH%
:complete
