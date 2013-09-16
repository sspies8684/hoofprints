#!/bin/sh 

PID=`cat ~/workspace/rs-testbed/tmp/target/quagga-0.99.15/etc/bgpd.pid`
VALUE=`top -n 1 -p $PID -b | grep bgpd  | awk '{print $9;}'`

if [ "$VALUE" = "" ]; then
	echo "NaN"
else
	echo $VALUE
fi

