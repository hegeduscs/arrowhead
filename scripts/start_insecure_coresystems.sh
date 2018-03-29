#!/bin/bash

cd ..

echo Starting Core Systems - wait 1 minute
#More sleep time between core systems might be needed on slower devices like a Raspberry Pi

cd serviceregistry_sql/target
nohup java -jar serviceregistry_sql-M4.jar -d -daemon > insecure_sr.log &
echo Service Registry started
sleep 10s

cd ../../authorization/target
nohup java -jar authorization-M4.jar -d -daemon > insecure_auth.log &
echo Authorization started
sleep 10s

cd ../../gateway/target
nohup java -jar gateway-M4.jar -d -daemon > insecure_gateway.log &
echo Gateway started
sleep 10s

cd ../../gatekeeper/target
nohup java -jar gatekeeper-M4.jar -d -daemon > insecure_gk.log &
echo Gatekeeper started
sleep 10s

cd ../../orchestrator/target
nohup java -jar orchestrator-M4.jar -d -daemon > insecure_orch.log &
echo Orchestrator started
sleep 10s
