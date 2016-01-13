@echo off

set numArgs=0

for %%x in (%*) do Set /A numArgs+=1

IF %1 == start (

	goto run

) ELSE IF %1 == stop (

	goto stop

) ELSE IF %1 == status (

	goto status

) ELSE (

	echo Unknown arguments' %*
)
goto eof



:run
if not exist "logs" mkdir logs
	start /B java -jar target/twitter-grapher-v1.0-fat.jar -conf config.json >.\logs\logs.txt 2>&1

   "%JAVA_HOME%/bin/jps.exe" -v > .\logs\processes.txt

   FOR /F "tokens=1,2" %%a IN (.\logs\processes.txt) DO (

	   IF %%b == twitter-grapher-v1.0-fat.jar (

	   		echo Server running under PID: %%a
	   		echo %%a > .\logs\pid.dat
	   )

)
goto eof


:stop
IF EXIST .\logs\pid.dat (

		  set /p pid=< .\logs\pid.dat 

		  IF "%pid%" == "" (
		  		echo No server running.
		  )ELSE (

		  	taskkill /f /pid %pid%
		  	IF EXIST .\logs\pid.dat del /F .\logs\pid.dat
		  	echo Server stoped successfully.

		  )

) ELSE (

		echo No server running.
)
goto eof

set numCheckRept=0

:status 
IF EXIST .\logs\pid.dat (

		  set /p pid=< .\logs\pid.dat 
		  IF %numCheckRept% EQU 0 (
		  	 set /A numCheckRept=numCheckRept+1
		  	 goto status
		  )

		  IF "%pid%" == "" (
		  		echo No server running.
		  )ELSE (
		  	echo Server running under PID: %pid%

		  )

) ELSE (

		echo No server running.
)


:eof
set /A numCheckRept=0
Exit /b