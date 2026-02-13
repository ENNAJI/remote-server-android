@echo off
echo ========================================
echo Compilation APK Remote Server Android
echo ========================================
echo.

echo [*] Verification de Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Java n'est pas installe!
    echo.
    echo Telechargez et installez Java JDK 11 ou superieur:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo [+] Java OK
echo.

echo [*] Telechargement de Gradle Wrapper...
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"
)
echo [+] Gradle Wrapper OK
echo.

echo [*] Compilation de l'APK...
echo    Cela peut prendre 5-10 minutes la premiere fois...
echo.
call gradlew.bat assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo [!] Erreur de compilation!
    echo.
    echo Solutions:
    echo 1. Verifiez que Java JDK est installe
    echo 2. Verifiez votre connexion Internet
    echo 3. Essayez: gradlew clean puis gradlew assembleDebug
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo [+] COMPILATION REUSSIE!
echo ========================================
echo.
echo L'APK est disponible ici:
echo app\build\outputs\apk\debug\app-debug.apk
echo.
echo Pour installer sur Android:
echo   adb install app\build\outputs\apk\debug\app-debug.apk
echo.
echo Ou transferez le fichier sur votre Android et installez-le manuellement.
echo.
pause
