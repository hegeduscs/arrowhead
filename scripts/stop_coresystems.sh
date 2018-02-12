#!/bin/sh
#run as sudo
pkill -f orchestrator
pkill -f authorization
pkill -f gatekeeper
pkill -f gateway
sleep 3s
pkill -f serviceregistry
echo Core systems killed