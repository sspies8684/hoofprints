#!/bin/sh 

PID=`cat ~/workspace/rs-testbed/tmp/target/bird-1.1.3/var/run/bird.pid`
PID=`expr $PID + 1`
VALUE=`top -n 1 -p $PID -b | grep bird  | awk '{print $9;}'`

if [ "$VALUE" = "" ]; then
	echo "NaN"
else
	echo $VALUE
fi

