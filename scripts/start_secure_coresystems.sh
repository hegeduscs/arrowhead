#!/bin/bash

cd ..

echo Starting Core Systems - wait 1 minute
#More sleep time between core systems might be needed on slower devices like a Raspberry Pi

cd serviceregistry_sql/target
nohup java -jar serviceregistry_sql-M3.jar -d -daemon -tls > secure_sr.log &
echo Service Registry started
sleep 10s

cd ../../authorization/target
nohup java -jar authorization-M3.jar -d -daemon -tls > secure_auth.log &
echo Authorization started
sleep 10s

cd ../../gateway/target
nohup java -jar gateway-M3.jar -d -daemon -tls > secure_gateway.log &
echo Gateway started
sleep 10s

cd ../../gatekeeper/target
nohup java -jar gatekeeper-M3.jar -d -daemon -tls > secure_gk.log &
echo Gatekeeper started
sleep 10s

cd ../../orchestrator/target
nohup java -jar orchestrator-M3.jar -d -daemon -tls > secure_orch.log &
echo Orchestrator started
sleep 10s