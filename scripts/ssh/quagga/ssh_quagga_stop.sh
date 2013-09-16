#!/bin/sh

. ~/workspace/rs-testbed/scripts/ssh/config


HOST=$1
PID=`ssh $HOST cat $PID_FILE`
ssh $HOST kill $PID