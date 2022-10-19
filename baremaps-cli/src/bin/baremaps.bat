@echo off
set dir=%~dp0%
set LIB="%dir%..\lib\*"
java -cp %LIB% ^
  -Dsun.stdout.encoding=UTF-8 ^
  -Dsun.err.encoding=UTF-8 ^
  org.apache.baremaps.cli.Baremaps %*
