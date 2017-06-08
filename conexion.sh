#!/bin/bash
HOST="45.79.82.119"
USER="root"
PASS="3htp.com2017"
CMD=$@

VAR=$(expect -c "
spawn ssh -o StrictHostKeyChecking=no $USER@$HOST $CMD
match_max 100000
expect \"*?assword:*\"
send -- \"$PASS\r\"
send -- \"\r\"
expect eof
")


echo "==============="
echo "$VAR"
