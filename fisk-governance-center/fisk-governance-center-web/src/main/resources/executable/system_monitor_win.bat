@echo off

REM Windows系统状态收集脚本

REM 查询操作系统版本
rem for /f "tokens=2 delims==" %%i in ('wmic os get Caption /value') do set os_version=%%i

REM 查看Windows操作系统ip地址
for /f "tokens=2 delims=:" %%i in ('ipconfig ^| findstr "IPv4"') do set ip_addr=%%i
rem echo ip_address: %ip_addr%

REM 查询系统运行时间
rem for /f "tokens=2 delims==" %%i in ('wmic os get LastBootUpTime /value') do set uptime=%%i
rem set "uptime=%uptime:~0,8% %uptime:~8,2%:%uptime:~10,2%:%uptime:~12,2%"

rem 获取当前时间
for /f "tokens=2 delims==" %%i in ('wmic os get LocalDateTime /value') do set current_datetime=%%i
rem echo current_datetime: %current_datetime%

rem 获取启动时间
for /f "tokens=2 delims==" %%i in ('wmic os get LastBootUpTime /value') do set boot_datetime=%%i
rem echo boot_datetime: %boot_datetime%

rem 提取日期和时间的各个部分
set "current_date=%current_datetime:~0,8%"
set "current_time=%current_datetime:~8,6%"
set "boot_date=%boot_datetime:~0,8%"
set "boot_time=%boot_datetime:~8,6%"

rem echo current_date: %current_date%
rem echo current_time: %current_time%
rem echo boot_date: %boot_date%
rem echo boot_time: %boot_time%

set /a "runtime_days=current_date - boot_date"
rem echo System has been running for: %runtime_days%

REM 查询CPU核数
for /f "tokens=2 delims==" %%i in ('wmic cpu get NumberOfCores /value') do set cpu_cores=%%i

REM 查询物理内存
for /f "skip=1 tokens=2 delims==" %%i in ('wmic memorychip get Capacity /value') do (
  set "rawTotal=%%i"
)
for /f %%i in ('powershell -Command "[math]::Round(%rawTotal% / 1024)"') do set "rawTotal=%%i"

REM 查询交换空间
rem 获取页面文件信息
for /f "tokens=1,2 delims== " %%A in ('wmic pagefile get AllocatedBaseSize^, CurrentUsage /value ^| findstr /r /c:"^AllocatedBaseSize=" /c:"^CurrentUsage="') do (
    if "%%A"=="AllocatedBaseSize" (
        set "allocatedBaseSize=%%B"
    ) else if "%%A"=="CurrentUsage" (
        set "currentUsage=%%B"
    )
)
set /a "swapTotal=allocatedBaseSize / 1024"
set /a "swapUsed=currentUsage /1024"

REM 检查系统CPU使用情况
rem 获取 CPU 使用率信息
for /f "skip=2 tokens=2 delims==," %%A in ('wmic cpu get loadpercentage /value') do (
    set "cpuUsage=%%A"
)

REM 查询内存使用情况
for /f "tokens=2 delims==" %%i in ('wmic os get TotalVisibleMemorySize /value') do (
    set totalMemory=%%i
)
for /f "tokens=2 delims==" %%i in ('wmic os get FreePhysicalMemory /value') do (
    set freeMemory=%%i
)
set /a rawUsed=totalMemory - freeMemory

REM 检查系统磁盘使用情况
rem for /f "skip=1 tokens=3,6 delims= " %%i in ('wmic logicaldisk get DeviceID^,Size^,FreeSpace') do (
rem     if "%%i"=="C:" (
rem         set totalSpace=%%j
rem         set freeSpace=%%k
rem     )
rem )
rem set /a usedSpace=totalSpace - freeSpace
rem set /a disk_usage=usedSpace * 100 / totalSpace

REM 构建JSON格式的输出
set json_output={
rem set json_output=%json_output%"Operating System Version":"%os_version%","
set json_output=%json_output%"ip":"%ip_addr%","
set json_output=%json_output%"upTime":"%runtime_days% days","
set json_output=%json_output%"cpuCores":"%cpu_cores%","

set json_output=%json_output%"rawTotal":"%rawTotal%","
set json_output=%json_output%"rawUsed":"%rawUsed%","
set json_output=%json_output%"swapTotal":"%swapTotal%","
set json_output=%json_output%"swapUsed":"%swapUsed%","

set json_output=%json_output%"cpuBusy":"%cpuUsage%"

rem set json_output=%json_output%"memory_usage":"%memory_usage%","
rem set json_output=%json_output%"disk_usage":"%disk_usage%""
set json_output=%json_output%}

REM 输出JSON
echo %json_output%

REM 使用curl发送POST请求
rem curl -X POST "http://192.168.11.130:8093/systemMonitor/saveSystemMonitor" -H "accept: */*" -H "Content-Type: application/json" -d "%json_output%"

