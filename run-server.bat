@echo off
cd /d "%~dp0"
echo Starting chat server...
echo Server launcher started at %date% %time% > server.log
javac -d out src\chat\*.java
powershell -NoProfile -ExecutionPolicy Bypass -Command "java -cp out chat.ChatServer 2>&1 | Tee-Object -FilePath server.log -Append"
pause
