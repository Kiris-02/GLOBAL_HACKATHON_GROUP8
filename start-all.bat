@echo off
title AI Career Coach - System Launcher
chcp 65001 >nul
echo ========================================================
echo        AI CAREER COACH - AUTOMATIC SYSTEM LAUNCHER
echo ========================================================
echo.

echo [1/3] Starting MySQL Docker container (Port 3308)...
docker start aicareer-mysql >nul 2>&1
if %errorlevel% neq 0 (
    docker-compose -f "%~dp0docker-compose.yml" up -d
)
echo      -> MySQL Database is running on port 3308!
echo.

echo [2/3] Starting Spring Boot Backend (Port 8080)...
cd /d "%~dp0backend"
start "AI Career Backend (Port 8080)" cmd /k "title Backend 8080 && java -jar target\AICareerCode-0.0.1-SNAPSHOT.jar"
echo      -> Backend started in separate window!
echo.

echo [3/3] Starting Frontend (Port 5173)...
cd /d "%~dp0frontend"
start "AI Career Frontend (Port 5173)" cmd /k "title Frontend 5173 && npm run dev -- --host 0.0.0.0 --port 5173"
echo      -> Frontend started in separate window!
echo.

echo ========================================================
echo   HE THONG DA KHOI DONG THANH CONG!
echo.
echo   💻 Tren may tinh:
echo      👉 http://localhost:5173
echo.
echo   📱 Tren dien thoai (ket noi chung WiFi):
echo      👉 Mo trinh duyet tren dien thoai: http://10.87.1.148:5173
echo.
echo   🌐 Neu muon tao link ra ngoai mang Internet:
echo      👉 Chay them file "start-tunnel.bat"
echo ========================================================
pause
