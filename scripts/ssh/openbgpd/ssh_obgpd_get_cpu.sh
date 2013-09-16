#!/bin/sh
. ~/workspace/rs-testbed/scripts/ssh/openbgpd/config

HOST=$1

PID=`ssh -v $HOST ps auxw | grep 'route decision engine' | grep -v grep | awk '{ print $2;}'`

if [ "x$PID" = "x" ]; then
	echo "NaN"
	exit 1
fi

calc()
{
        CPUTIME=`ssh $HOST ps -p $PID -o cputime | tail -n 1`
        MINS=`echo $CPUTIME | sed -e 's,:.*,,'`
        SECS=`echo $CPUTIME | sed -e 's,.*:,,' -e 's,\..*,,'`
        HSECS=`echo $CPUTIME | sed -e 's,.*\.,,'`
        return `expr $MINS \* 6000 + $SECS \* 100 + $HSECS`
}


calc
TICK1=$?
sleep 1 
calc
TICK2=$?
expr "(" $TICK2 - $TICK1 ")" 
