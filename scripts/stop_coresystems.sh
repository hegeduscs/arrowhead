#!/bin/bash
#More sleep time between these commands might be needed on slower devices like a Raspberry Pi (because of the database accesses)
echo Shutting down Core Systems
pkill -f orchestrator
pkill -f authorization
pkill -f gatekeeper
pkill -f gateway
sleep 5s
pkill -f serviceregistry
echo Core systems killed