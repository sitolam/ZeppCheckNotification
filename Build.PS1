$env:JAVA_HOME = "tools\jdk17\jdk-17.0.13+11";
$env:ANDROID_HOME = "tools\android-sdk"; 
.\gradlew.bat assembleRelease
pause