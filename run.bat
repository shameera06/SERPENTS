@echo off
title Sha Reptiles AI Diagnostic & Husbandry System - Java Spring Boot Server
echo ==============================================================================
echo       SHA REPTILES AI DIAGNOSTIC ^& HUSBANDRY MANAGEMENT SYSTEM
echo                   Java Spring Boot + Maven + Thymeleaf
echo ==============================================================================
echo.

set "PATH=%JAVA_HOME%\bin;%~dp0apache-maven-3.9.6\bin;%PATH%"

echo [INFO] Java Home: %JAVA_HOME%
echo [INFO] Starting Spring Boot Server on http://localhost:8000 ...
echo.

call "%~dp0apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run

pause
