#!/bin/sh

. ~/workspace/rs-testbed/scripts/ssh/config

HOST=$1
PID=`ssh $HOST cat $PID_FILE`
ssh $HOST ps aux | grep $PID | grep bgpd > /dev/null
echo `expr 1 - $?`
  