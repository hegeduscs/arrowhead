#!/bin/bash
#run as sudo

cd /home/pi/arrowhead/target

if [ -e coresystem.log ]
then
    rm coresystem.log
fi

nohup java -jar arrowhead_core-M3-lightweight.jar -d -daemon > /home/pi/arrowhead/target/coresystem.log &