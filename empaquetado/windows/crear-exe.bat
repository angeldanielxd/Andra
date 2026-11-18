@echo off
rem ==================================================================
rem  Andra - crea el .exe para Windows con jpackage.
rem  Uso: doble clic en este archivo.
rem  Necesita: JDK 21 o superior (trae jpackage).
rem  Opcional: WiX Toolset 3.14 para crear tambien el instalador.
rem  Si cambias la version en pom.xml, cambiala tambien aqui abajo.
rem ==================================================================
setlocal EnableExtensions
cd /d "%~dp0"
title Andra - crear .exe para Windows

set "VERSION=1.3.0"
set "JAR_NOMBRE=andra-javafx-%VERSION%.jar"
set "SALIDA=%~dp0salida"
set "ICONO=%~dp0Andra.ico"
set "UUID=6f1c3c2e-4b7a-4f2a-9c51-2d8e7a1b9c40"
set "MAIN=org.springframework.boot.loader.launch.JarLauncher"
set "DESCRIPCION=Andra - tus tramites mas simples"

echo.
echo  Andra %VERSION% - empaquetado para Windows
echo  ===========================================
echo.

rem --- 1. JAR: junto a este archivo (kit) o en target del proyecto ---
set "JAR=%~dp0%JAR_NOMBRE%"
if not exist "%JAR%" set "JAR=%~dp0..\..\target\%JAR_NOMBRE%"
if not exist "%JAR%" goto :sin_jar
if not exist "%ICONO%" goto :sin_icono

rem --- 2. jpackage: primero JAVA_HOME, luego el PATH ---
set "JPACKAGE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" set "JPACKAGE=%JAVA_HOME%\bin\jpackage.exe"
if not defined JPACKAGE for /f "delims=" %%i in ('where jpackage.exe 2^>nul') do if not defined JPACKAGE set "JPACKAGE=%%i"
if not defined JPACKAGE goto :sin_jdk

set "JDK="
set "JDKV="
"%JPACKAGE%" --version > "%TEMP%\andra-jdk.txt" 2>nul
set /p JDKV=<"%TEMP%\andra-jdk.txt"
del "%TEMP%\andra-jdk.txt" >nul 2>&1
if not defined JDKV goto :sin_jdk
for /f "tokens=1 delims=." %%v in ("%JDKV%") do set "JDK=%%v"
if not defined JDK goto :sin_jdk
if %JDK% LSS 21 goto :jdk_viejo

echo  JDK %JDKV%: "%JPACKAGE%"
echo  JAR: "%JAR%"
echo.

rem --- 3. Carpeta de trabajo ---
if exist "%SALIDA%" rmdir /s /q "%SALIDA%"
mkdir "%SALIDA%\entrada"
copy /y "%JAR%" "%SALIDA%\entrada\%JAR_NOMBRE%" >nul
if errorlevel 1 goto :error_copia

echo [1/3] Creando la aplicacion portable Andra.exe (tarda 1 a 2 minutos)...
"%JPACKAGE%" --type app-image --name Andra --app-version %VERSION% --vendor Andra ^
  --input "%SALIDA%\entrada" --main-jar %JAR_NOMBRE% --main-class %MAIN% ^
  --icon "%ICONO%" --description "%DESCRIPCION%" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --dest "%SALIDA%"
if errorlevel 1 goto :error_jpackage

echo [2/3] Comprimiendo la version portable...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Compress-Archive -Path '%SALIDA%\Andra' -DestinationPath '%SALIDA%\Andra-%VERSION%-windows-portable.zip' -Force"
if errorlevel 1 echo [AVISO] No se pudo crear el zip portable. La carpeta salida\Andra funciona igual.

echo [3/3] Creando el instalador Andra-%VERSION%.exe (necesita WiX Toolset)...
"%JPACKAGE%" --type exe --name Andra --app-version %VERSION% --vendor Andra ^
  --input "%SALIDA%\entrada" --main-jar %JAR_NOMBRE% --main-class %MAIN% ^
  --icon "%ICONO%" --description "%DESCRIPCION%" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --win-menu --win-menu-group Andra --win-shortcut --win-dir-chooser --win-per-user-install ^
  --win-upgrade-uuid %UUID% ^
  --dest "%SALIDA%"
if errorlevel 1 (set "INSTALADOR=no") else (set "INSTALADOR=si")

rmdir /s /q "%SALIDA%\entrada" >nul 2>&1

echo.
echo  ===========================================
echo  Listo. Archivos en la carpeta salida:
echo.
echo    Andra\Andra.exe
echo        Version portable: abrela con doble clic. No separes Andra.exe de su carpeta.
if exist "%SALIDA%\Andra-%VERSION%-windows-portable.zip" echo    Andra-%VERSION%-windows-portable.zip
if exist "%SALIDA%\Andra-%VERSION%-windows-portable.zip" echo        La misma version portable comprimida, para compartir.
if "%INSTALADOR%"=="si" echo    Andra-%VERSION%.exe
if "%INSTALADOR%"=="si" echo        Instalador con accesos en el menu Inicio y el escritorio.
if "%INSTALADOR%"=="no" echo    [AVISO] No se creo el instalador; lo mas probable es que falte WiX Toolset 3.14.
if "%INSTALADOR%"=="no" echo            https://github.com/wixtoolset/wix3/releases  - luego vuelve a ejecutar este archivo.
echo.
start "" explorer "%SALIDA%"
goto :fin

:sin_jar
echo [ERROR] No encuentro %JAR_NOMBRE%.
echo         Si usas el kit: descomprimelo primero con clic derecho, Extraer todo.
echo         Si usas el proyecto: compilalo en Windows con  mvn -DskipTests package
echo         o desde IntelliJ: panel Maven, Lifecycle, package.
goto :fin_error

:sin_icono
echo [ERROR] No encuentro Andra.ico junto a este archivo.
goto :fin_error

:sin_jdk
echo [ERROR] No encuentro jpackage. Instala el JDK 21 o superior, por ejemplo Temurin:
echo         https://adoptium.net/temurin/releases/  - Windows, x64, JDK, archivo .msi
echo         En el instalador activa "Add to PATH" y "Set JAVA_HOME variable".
echo         Luego cierra esta ventana y vuelve a ejecutar este archivo.
goto :fin_error

:jdk_viejo
echo [ERROR] Tu JDK es la version %JDKV%. Andra necesita el JDK 21 o superior.
echo         https://adoptium.net/temurin/releases/
goto :fin_error

:error_copia
echo [ERROR] No se pudo preparar la carpeta salida.
goto :fin_error

:error_jpackage
echo [ERROR] jpackage no pudo crear la aplicacion. Revisa los mensajes de arriba.
goto :fin_error

:fin_error
echo.
echo  No se completo el empaquetado.
echo.
pause
exit /b 1

:fin
pause
exit /b 0
