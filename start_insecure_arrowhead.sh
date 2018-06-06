#!/bin/bash
#run as sudo

cd /home/pi/arrowhead/target

if [ -e coresystem.log ]
then
    rm coresystem.log
fi

nohup java -jar arrowhead_core-4.0-lw.jar -d -daemon > /home/pi/arrowhead/target/coresystem.log &