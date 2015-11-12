#!/bin/bash

if [ $# -eq 0 ]; then
	echo "Action argument not found."
	echo "Possible arguments: start/stop/status"
else
	ACTION=$1
fi


function run {

	nohup java -jar ./target/twitter-grapher-v1.0-fat.jar -conf config.json > ./logs/logs.txt &
	echo $! > ./logs/pid.dat
	echo "Server started succesfully..."
}


if [[ $ACTION == 'start' ]]; then
	
	if [ ! -s ./logs/pid.dat ]; then

		if [ -s ./logs/pid.dat ]; then
			
			PID=`cat ./logs/pid.dat`
			if ! ps -p $PID > null ; then
				run
			fi

		else
			run
		fi
	else
		PID=`cat ./logs/pid.dat`
		echo "Service already running under PID $PID"
	fi

elif [[ $ACTION == 'stop' ]]; then

		PID=`cat ./logs/pid.dat`
		
		if [ ! -z "$PID" ]; then

			kill -9 $PID
			> ./logs/pid.dat
			echo "Service stoped successfully."

		else 

			echo "No Server Running..."
		fi

elif [[ $ACTION == 'status' ]]; then

			if [ -s ./logs/pid.dat ]; then

				PID=`cat ./logs/pid.dat`

				if ps -p $PID > null ;then
					echo "Service running under PID $PID"
				else

					> ./logs/pid.dat
					echo "No Service Running."
				fi
			else
				echo "No Service Running."
			fi
else
	echo "Unknown arguments' $*"
fi

