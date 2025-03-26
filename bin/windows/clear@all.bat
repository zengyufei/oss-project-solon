chcp 65001
@echo off

echo 清理
cd %~dp0
cd..
cd..
call mvn -U -T 1C clean -Dmaven.test.skip=true
cd ./bin/windows
pause
