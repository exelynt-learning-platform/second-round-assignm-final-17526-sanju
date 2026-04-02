@echo off
REM E-Commerce Backend Quick Start Script
REM This script automates the setup process

echo.
echo ======================================
echo  E-Commerce Backend Quick Start
echo  Version 1.0.0
echo ======================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 8+ and add it to your PATH
    pause
    exit /b 1
)

REM Display Java version
echo [INFO] Java version:
java -version
echo.

REM Display Maven version
echo [INFO] Maven version:
mvn -v
echo.

REM Option menu
echo What would you like to do?
echo.
echo 1. Build the project (compile only)
echo 2. Run all tests
echo 3. Build and run tests
echo 4. Start the application
echo 5. Clean build (remove target folder)
echo 6. Full setup (clean + build + tests)
echo.

set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" (
    echo.
    echo [INFO] Building project...
    call mvnw.cmd clean build
    if %ERRORLEVEL% equ 0 (
        echo [SUCCESS] Build completed successfully!
    ) else (
        echo [ERROR] Build failed!
    )
) else if "%choice%"=="2" (
    echo.
    echo [INFO] Running tests...
    call mvnw.cmd clean test
    if %ERRORLEVEL% equ 0 (
        echo [SUCCESS] All tests passed! (19/19)
    ) else (
        echo [ERROR] Some tests failed!
    )
) else if "%choice%"=="3" (
    echo.
    echo [INFO] Building project and running tests...
    call mvnw.cmd clean build
    if %ERRORLEVEL% equ 0 (
        echo [SUCCESS] Build and tests completed successfully!
    ) else (
        echo [ERROR] Build or tests failed!
    )
) else if "%choice%"=="4" (
    echo.
    echo [INFO] Starting application...
    echo [WARN] Make sure MySQL is running and database is set up!
    echo [INFO] Application will start on http://localhost:8080
    echo.
    call mvnw.cmd spring-boot:run
) else if "%choice%"=="5" (
    echo.
    echo [INFO] Cleaning build artifacts...
    call mvnw.cmd clean
    if exist target (
        rmdir /s /q target
    )
    echo [SUCCESS] Clean completed!
) else if "%choice%"=="6" (
    echo.
    echo [INFO] Starting full setup...
    echo [STEP 1] Cleaning build artifacts...
    call mvnw.cmd clean
    
    echo [STEP 2] Building project...
    call mvnw.cmd build
    
    echo [STEP 3] Running tests...
    call mvnw.cmd test
    
    if %ERRORLEVEL% equ 0 (
        echo.
        echo [SUCCESS] Full setup completed!
        echo [INFO] Next steps:
        echo   1. Ensure MySQL is running
        echo   2. Run database setup: mysql -u root -p ^< database_setup.sql
        echo   3. Start application: mvnw.cmd spring-boot:run
        echo   4. API available at: http://localhost:8080/api
    ) else (
        echo [ERROR] Setup failed during tests!
    )
) else (
    echo [ERROR] Invalid choice. Please run again and select 1-6.
)

echo.
pause
