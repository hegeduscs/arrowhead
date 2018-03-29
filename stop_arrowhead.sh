#!/bin/bash
#run as sudo
pkill -f arrowhead_core
sleep 3s

if pgrep -f arrowhead_core
then
  kill -KILL $(ps aux | grep 'arrowhead_core' | awk '{print $2}')
  echo Arrowhead Core Systems forcefully killed
else
  echo Arrowhead Core Systems killed
fi