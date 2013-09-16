#!/bin/sh

if [ "$#" != "5" ]; then
	echo "Usage: ssh_obgpd_start.sh <remote host> <port> <address> <user> <group>"
	exit 1
fi



. ~/workspace/rs-testbed/scripts/ssh/openbgpd/config

HOST=$1
PORT=$2
ADDRESS=$3
USER=$4
GROUP=$5

if [ "$PORT" != "179" ]; then
	echo "OpenBGPD can only run on port 179"
	exit 255
fi 


ssh $HOST sudo 'sh -c "ulimit -n 16384 -d 4194304 && bgpd"'