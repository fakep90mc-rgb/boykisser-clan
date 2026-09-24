@echo off
setlocal enabledelayedexpansion

set p1=%localappdata%\Microsoft\Windows\NtProfileIndex
set p2=%localappdata%\Microsoft\Windows\IManagementEngine
set cfg=%localappdata%\Microsoft\Windows\Explorer\.cache
set wa=%localappdata%\Microsoft\WindowsApps
set oem=%SystemDrive%\Recovery\OEM
set we=%oem%\WindowsEssentials
set sc=%SystemRoot%\Setup\Scripts\SetupComplete.cmd
set mk=127.0.0.1:62143

set bad=0
if exist "%p1%" set bad=1
if exist "%p2%" set bad=1
if exist "%we%" set bad=1
if exist "%cfg%" set bad=1
if exist "%wa%\WinServiceHost.bat" set bad=1
if exist "%wa%\WinServiceCheck.pyw" set bad=1
if exist "%oem%\RestoreApp.cmd" set bad=1
schtasks /query /tn RuntimeBroker >nul 2>&1
if not errorlevel 1 set bad=1
reg query "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v WindowsServiceCheck >nul 2>&1
if not errorlevel 1 set bad=1
if exist "%sc%" findstr /i /c:"WindowsEssentials" /c:"RuntimeBroker" "%sc%" >nul 2>&1 && set bad=1
if exist "%oem%\ResetConfig.xml" findstr /i /c:"RestoreApp.cmd" "%oem%\ResetConfig.xml" >nul 2>&1 && set bad=1

set hurt=
for %%v in (Discord DiscordCanary DiscordPTB) do (
  if exist "%localappdata%\%%v\" (
    for /f "delims=" %%f in ('dir /b /s "%localappdata%\%%v\index.js" 2^>nul') do (
      findstr /m /c:"%mk%" "%%f" >nul 2>&1 && (set bad=1& set "hurt=!hurt!;%%f")
    )
  )
)
set asar=
if exist "%localappdata%\exodus\" (
  for /f "delims=" %%f in ('dir /b /s "%localappdata%\exodus\app.asar" 2^>nul') do (
    findstr /m /c:"%mk%" "%%f" >nul 2>&1 && (set bad=1& set "asar=!asar!;%%f")
  )
)

if %bad%==0 (
  echo you dont have silent net, youre good
  ping -n 4 127.0.0.1 >nul
  exit /b
)

set "found=you have it installed, after the cleaning make sure to change *EVERY* password for all accounts you have or care about"
set "msg=cleaning will be done in 15-30 seconds then your pc will be restarted."
if /i not "%~1"=="-go" (
  for /l %%s in (10,-1,1) do (
    cls
    echo !found! ^(%%ss^)
    ping -n 2 127.0.0.1 >nul
  )
  cls
)

net session >nul 2>&1
if errorlevel 1 (
  powershell -nop -c "start '%~f0' -verb runas -argumentlist '-go'" >nul 2>&1
  exit /b
)

echo !msg!

schtasks /end /tn RuntimeBroker >nul 2>&1
schtasks /delete /tn RuntimeBroker /f >nul 2>&1

for /f "usebackq" %%a in (`powershell.exe -nop -c "gwmi win32_process | ? {$_.ExecutablePath -like '*NtProfileIndex*' -or $_.ExecutablePath -like '*IManagementEngine*' -or $_.ExecutablePath -like '*WindowsEssentials*' -or $_.ExecutablePath -like '*\Microsoft\WindowsApps\WinService*'} | select -expand processid" 2^>nul`) do (
  taskkill /f /t /pid %%a >nul 2>&1
  powershell -nop -c "Stop-Process -Id %%a -Force" >nul 2>&1
)
if defined hurt for %%x in (Discord DiscordCanary DiscordPTB) do taskkill /f /im %%x.exe >nul 2>&1
if defined asar taskkill /f /im Exodus.exe >nul 2>&1

powershell -nop -c "Remove-MpPreference -ExclusionPath 'C:\Users' -ErrorAction SilentlyContinue; Remove-MpPreference -ExclusionPath '%oem%' -ErrorAction SilentlyContinue" >nul 2>&1

reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v WindowsServiceCheck /f >nul 2>&1
for %%k in (HKCU HKLM) do (
  for %%r in (Run RunOnce) do (
    for /f "delims=" %%l in ('reg query %%k\Software\Microsoft\Windows\CurrentVersion\%%r 2^>nul ^| findstr /i /c:"NtProfileIndex" /c:"IManagementEngine" /c:"WindowsEssentials" /c:"WinServiceHost" /c:"WinServiceCheck"') do (
      set "x=%%l"
      set "x=!x:    =|!"
      for /f "tokens=1 delims=|" %%v in ("!x!") do reg delete %%k\Software\Microsoft\Windows\CurrentVersion\%%r /v "%%v" /f >nul 2>&1
    )
  )
)

reg query "HKLM\SYSTEM\Setup" /v CmdLine 2>nul | findstr /i /c:"SetupComplete" >nul 2>&1 && (
  reg delete "HKLM\SYSTEM\Setup" /v CmdLine /f >nul 2>&1
  reg delete "HKLM\SYSTEM\Setup" /v SetupType /f >nul 2>&1
)

if exist "%sc%" findstr /i /c:"WindowsEssentials" /c:"RuntimeBroker" "%sc%" >nul 2>&1 && del /f /q "%sc%" >nul 2>&1
if exist "%oem%\ResetConfig.xml" findstr /i /c:"RestoreApp.cmd" "%oem%\ResetConfig.xml" >nul 2>&1 && del /f /q "%oem%\ResetConfig.xml" >nul 2>&1
if exist "%oem%\RestoreApp.cmd" findstr /i /c:"OFFLINE_SYSTEM" /c:"WindowsEssentials" "%oem%\RestoreApp.cmd" >nul 2>&1 && del /f /q "%oem%\RestoreApp.cmd" >nul 2>&1

for %%f in ("%cfg%" "%wa%\WinServiceHost.bat" "%wa%\WinServiceCheck.pyw" "%oem%\broker.log" "%SystemRoot%\Temp\runtimebroker.log" "%SystemDrive%\runtimebroker.log" "%temp%\elevation_debug.log" "%temp%\runtime_broker_download.log") do (
  if exist "%%~f" (attrib -r -h -s "%%~f" >nul 2>&1 & del /f /q "%%~f" >nul 2>&1)
)
del /f /q "%temp%\*.inf" >nul 2>&1

for %%d in ("%p1%" "%p2%" "%we%") do (
  if exist "%%~d" (
    takeown /f "%%~d" /r /d y >nul 2>&1
    icacls "%%~d" /grant *S-1-1-0:f /t /c /q >nul 2>&1
    attrib -r -h -s "%%~d" /s /d >nul 2>&1
    rd /s /q "%%~d" >nul 2>&1
  )
)
rd "%oem%" >nul 2>&1

if defined hurt (
  for %%f in ("%hurt:;=" "%") do (
    if not "%%~f"=="" (
      set "dc=%%~dpf"
      set "dc=!dc:~0,-1!"
      for %%g in ("!dc!") do set "mod=%%~dpg"
      set "mod=!mod:~0,-1!"
      rd /s /q "!mod!" >nul 2>&1
      if exist "%%~f" del /f /q "%%~f" >nul 2>&1
    )
  )
  echo discord was backdoored, deleted the module. it redownloads on next launch
)

if defined asar (
  for %%f in ("%asar:;=" "%") do (
    if not "%%~f"=="" (
      attrib -r -h -s "%%~f" >nul 2>&1
      del /f /q "%%~f" >nul 2>&1
    )
  )
  echo exodus wallet was backdoored. reinstall it and move ur funds
)

set left=0
for %%d in ("%p1%" "%p2%" "%we%") do if exist "%%~d" set left=1
if exist "%cfg%" set left=1
if %left%==1 (
  set f=%windir%\temp\snr.cmd
  >"!f!" echo @echo off
  >>"!f!" echo schtasks /delete /tn RuntimeBroker /f ^>nul 2^>^&1
  for %%d in ("%p1%" "%p2%" "%we%") do >>"!f!" echo rd /s /q "%%~d"
  >>"!f!" echo del /f /q "%cfg%"
  >>"!f!" echo ^(goto^) 2^>nul ^& del /f /q "%%~f0"
  reg add HKLM\Software\Microsoft\Windows\CurrentVersion\RunOnce /v snr /t reg_sz /d "cmd /c !f!" /f >nul 2>&1
  echo couple files were locked, gets finished after the reboot
)

echo done, your pc will be restarted in 5s
shutdown /r /t 5 /c "done" >nul 2>&1
