#!/bin/bash

echo Starting Core Systems - wait 1 minute
#More sleep time between core systems might be needed on slower devices like a Raspberry Pi

cd ~/arrowhead/serviceregistry_sql/target
rm nohup.out
nohup java -jar serviceregistry_sql-M3.jar -d -daemon -m both &
sleep 10s

cd ~/arrowhead/authorization/target
rm nohup.out
nohup java -jar authorization-M3.jar -d -daemon -m both &
echo Authorization started
sleep 10s

cd ~/arrowhead/gateway/target
rm nohup.out
nohup java -jar gateway-M3.jar -d -daemon -m both &
echo Gateway started
sleep 10s

cd ~/arrowhead/gatekeeper/target
rm nohup.out
nohup java -jar gatekeeper-M3.jar -d -daemon -m both &
echo Gatekeeper started
sleep 10s

cd ~/arrowhead/orchestrator/target
rm nohup.out
nohup java -jar orchestrator-M3.jar -d -daemon -m both &
echo Orchestrator started
sleep 10s
