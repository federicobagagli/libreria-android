@echo off
cd /d "C:\Sviluppo\Progetti\AndroidProjects\LibraryApp"
echo 🛠️ Avvio build con log dettagliato...
call gradlew.bat app:assembleDebug --stacktrace --info
pause
