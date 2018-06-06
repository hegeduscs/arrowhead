#!/bin/bash
#run as sudo

echo Starting Arrowhead Core Systems - wait 15-20 seconds

cd target

if [ -e nohup.out ]
then
    rm nohup.out
fi

nohup java -jar arrowhead_core-4.0-lw.jar -d -daemon -tls &
sleep 20s

echo Arrowhead is running... Check nohup.out to make sure exceptions did not occur!
