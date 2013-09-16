#!/bin/bash

. ~/workspace/rs-testbed/scripts/ssh/openbgpd/config

HOST=$1
case "$2" in
startup)
	HOSTNAME=$3
	PASSWORD=$4
	ASN=$5
	ADDRESS=$6

	cat << EOF > $TEMP_FILE

AS $ASN
router-id $ADDRESS
fib-update no
log updates

# deny from any
# deny to any
	
EOF
	;;
add_neighbor)
	ADDRESS=$3
	ASN=$4
	RSCLIENT=$5
	
	if [ "$6" = "true" ]; then
		TRANSPARENT="transparent-as yes"
	fi
	
	shift 6
	DESCRIPTION=$*

	cat << EOF >> $TEMP_FILE
neighbor $ADDRESS  {
	remote-as $ASN
	descr "$DESCRIPTION"
	announce all
	announce IPv4 unicast
	announce IPv6 unicast
	softreconfig in no
	softreconfig out no
	$TRANSPARENT 
	passive
}

EOF
	;;
add_global_prefix_filter)
	PREFIX=$3
	
	if [ "$5" = "-1" ]; then
		LEN="prefixlen 0 -"
	else
		LEN="prefixlen $5 -"
	fi
	
	if [ "$4" = "-1" ]; then
		LEN="${LEN} 32"
	else
		LEN="${LEN} $4"
	fi
	
	if [[ "$4" == "-1" && "$5" == "-1" ]]; then
		LEN=""
	fi

	
	if [ "$6" = "true" ]; then
		ALLOWED="allow"
	else
		ALLOWED="deny"
	fi

	cat << EOF >> $TEMP_FILE
$ALLOWED quick to any prefix $PREFIX $LEN
EOF
	;;
	
add_as_filter)
	PEER=$3
	SOURCE_AS=$4
	if [ "$5" = "true" ]; then
		ALLOWED="allow"
	else
		ALLOWED="deny"
	fi	
	
	cat << EOF >> $TEMP_FILE
$ALLOWED quick from $PEER source-as $SOURCE_AS
EOF
	;;
	
add_neighbor_prefix_filter)
	if [ "$3" = "in" ]; then
		DIRECTION="from"
	else
		DIRECTION="to"
	fi
	
	PEER=$4
	PREFIX=$5
	
	if [ "$7" = "-1" ]; then
		LEN="prefixlen 0 -"
	else
		LEN="prefixlen $7 -"
	fi
	
	if [ "$6" = "-1" ]; then
		LEN="${LEN} 32"
	else
		LEN="${LEN} $6"
	fi

	if [[ "$6" == "-1" && "$7" == "-1" ]]; then
		LEN=""
	fi
	
	if [ "$8" = "true" ]; then
		ALLOWED="allow"
	else
		ALLOWED="deny"
	fi

	cat << EOF >> $TEMP_FILE
$ALLOWED quick $DIRECTION $PEER prefix $PREFIX $LEN 
EOF
	;;
deploy)
	scp $TEMP_FILE $HOST:$TEMP_FILE
	ssh $HOST sudo cp $TEMP_FILE $CONFIG_FILE
esac
