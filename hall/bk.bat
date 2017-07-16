@echo off
rem by KY, 21:06 2014/7/9

title I'm backing up stuff
set jdk=C:\Java\jdk1.8.0_05
set from=D:\Gitstuff\SIRAP\mas
set target=M:\BKMas
set script=%from%\hall
set other=E:\KDB\libs

dir %target%

set quitSymbol=q
set /p name=create a version(%quitSymbol% to quit):
if "%name%"=="%quitSymbol%" goto quit
if "%name%"=="%" goto quit

set what=sirap-basic
set what=%what%,sirap-common
set what=%what%,sirap-db
set what=%what%,sirap-extractor
set what=%what%,sirap-executor
set what=%what%,sirap-ldap
set what=%what%,sirap-geek
set what=%what%,sirap-bible

set where=%target%\%name%

if exist %where% (goto clean) else (goto create)
:clean
del /s /q %where%\*.*
goto mvn

:create
md %where%
md %where%\jars

:mvn
cd /d %from%
set JAVA_HOME=%jdk%
set mvnResult=mvn.result.txt
echo maven is building...
call mvn -Dmaven.test.skip=true clean install -P s>%mvnResult%
findstr "BUILD SUCCESS" "mvn.result.txt"&&goto cpy||goto bad

:bad
type %mvnResult%
echo "oops, build failed."
goto end

:cpy
for %%i in (%what%) do copy %from%\%%i\target\*SHOT.jar %where%\jars
copy %from%\sirap-security\target\*.jar %where%\jars
copy %other%\mas.wav %where%\jars
copy %script%\ship.properties %where%
copy %script%\ship.bat %where%
xcopy /r /y %where%\jars\*.* %target%\latestjars

:status
cd /d %where%
dir

:app
call ship.bat
goto end

:quit
echo quit, bye

:end