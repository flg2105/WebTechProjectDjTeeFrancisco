@echo off
setlocal

set WRAPPER_DIR=%~dp0
set MAVEN_PROJECTBASEDIR=%WRAPPER_DIR%
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

if not exist "%MAVEN_WRAPPER_PROPERTIES%" (
  echo Missing %MAVEN_WRAPPER_PROPERTIES%
  exit /b 1
)

if not exist "%MAVEN_WRAPPER_JAR%" (
  set WRAPPER_VERSION=3.3.2
  set MVNW_REPOURL=https://repo.maven.apache.org/maven2
  set WRAPPER_JAR_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/%WRAPPER_VERSION%/maven-wrapper-%WRAPPER_VERSION%.jar
  if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "try { (New-Object Net.WebClient).DownloadFile('%WRAPPER_JAR_URL%', '%MAVEN_WRAPPER_JAR%') } catch { exit 1 }"
  if errorlevel 1 (
    echo Failed to download Maven wrapper jar.
    exit /b 1
  )
)

java -classpath "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %*
