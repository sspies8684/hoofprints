#!/bin/sh

if [ "$#" != "5" ]; then
	echo "Usage: ssh_quagga_start.sh <remote host> <port> <address> <user> <group>"
	exit 1
fi


. ~/workspace/rs-testbed/scripts/ssh/config

HOST=$1
PORT=$2
ADDRESS=$3
USER=$4
GROUP=$5


ssh $HOST $BGPD -d -l $ADDRESS -p $PORT -u $USER -g $GROUP