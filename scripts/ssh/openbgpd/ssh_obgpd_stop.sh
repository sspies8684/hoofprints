#!/bin/sh

. ~/workspace/rs-testbed/scripts/ssh/openbgpd/config

HOST=$1
PID=`ssh $HOST ps aux | grep bgp | grep parent | awk '{ print $2; }'`
ssh $HOST sudo kill $PID