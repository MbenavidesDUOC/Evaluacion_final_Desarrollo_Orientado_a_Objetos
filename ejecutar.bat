@echo off
cd /d "%~dp0"
java -jar distiblioteca-escolar.jar
if errorlevel 1 pause
