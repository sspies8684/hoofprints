#!/bin/sh

. ~/workspace/rs-testbed/scripts/ssh/config

HOST=$1
case "$2" in
startup)
	HOSTNAME=$3
	PASSWORD=$4
	ASN=$5
	ADDRESS=$6

	cat << EOF > $TEMP_FILE

hostname $HOSTNAME
password $PASSWORD
router bgp $ASN
	bgp router-id $ADDRESS	
	
EOF
	;;
add_neighbor)
	ADDRESS=$3
	ASN=$4
	RSCLIENT=$5
	shift 5
	DESCRIPTION=$*

	cat << EOF >> $TEMP_FILE
	neighbor $ADDRESS remote-as $ASN
	neighbor $ADDRESS description $DESCRIPTION
EOF
	;;
deploy)
	scp $TEMP_FILE $HOST:$CONFIG_FILE
esac
