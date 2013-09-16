#!/bin/sh

. ~/workspace/rs-testbed/scripts/ssh/openbgpd/config

HOST=$1
ssh $HOST [ -e "/var/run/bgpd.sock" ]
echo `expr 1 - $?`
  