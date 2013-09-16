#!/bin/sh 

PID=`cat ~/workspace/rs-testbed/tmp/target/quagga-0.99.15/etc/bgpd.pid`
VALUE=`ps -p $PID -o rss=`

if [ "$VALUE" = "" ]; then
	echo "NaN"
else
	echo $VALUE
fi

