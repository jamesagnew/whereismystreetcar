#!/bin/sh
# This script simulates the GWT cron service for local testing

while true
do 
   echo -n "`date`: "; 
   curl http://127.0.0.1:8888/cron/updateVehicles; 
   echo; 
   sleep 40; 
done
