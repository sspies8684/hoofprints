#!/bin/sh
. ~/workspace/rs-testbed/scripts/ssh/openbgpd/config

HOST=$1

PIDS=`ssh $HOST ps aux | grep bgpd  | grep -v grep  | awk '{ print $2; }' | tr '\n' ' '`
if [ "x$PIDS" = "x" ]; then
	echo "NaN"
	exit 1
fi

TOTAL=0
for p in $PIDS; do
	MEM=`ssh $HOST ps -p $p -o rss= | tail -n 1`
	TOTAL=`expr $TOTAL + $MEM`
done

echo $TOTAL