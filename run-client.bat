@echo off
cd /d "%~dp0"
javac -d out src\chat\*.java
java -cp out chat.ChatClient localhost 5000
pause
