#!/bin/sh

. ~/workspace/rs-testbed/scripts/ssh/config

HOST=$1
PID=`ssh $HOST cat $PID_FILE`
VALUE=`ssh $HOST top -n 1 -p $PID -b | grep bgpd  | awk '{print $9;}'`

if [ "$VALUE" = "" ]; then
	echo "NaN"
else
	echo $VALUE
fi
