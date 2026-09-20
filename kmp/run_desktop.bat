@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
cd /d "d:\Things\Padaippugal\Nadappil\Udukkai\migrate"
call gradlew.bat :composeApp:run
