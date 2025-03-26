chcp 65001
@echo off

echo 打包
cd %~dp0
cd..
cd..
call mvn -U -T 1C clean package -Dmaven.test.skip=true
cd ./bin/windows
pause
