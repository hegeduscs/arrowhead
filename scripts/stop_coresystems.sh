#!/bin/sh
#run as sudo
#More sleep time between these commands might be needed on slower devices like a Raspberry Pi (because of the database accesses)
pkill -f orchestrator
pkill -f authorization
pkill -f gatekeeper
pkill -f gateway
sleep 3s
pkill -f serviceregistry
echo Core systems killed