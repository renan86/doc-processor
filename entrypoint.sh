#!/bin/sh
set -x

JAVA_EXECUTABLE=java
JAVA_ARGS="-Djava.security.egd=file:/dev/./urandom -jar ${APP_ARTIFACT}"

warn() {
  echo "$*"
}

# ------------------------------------------------------------------------------
# Increase the maximum file descriptors.
#
MAX_FD_LIMIT=`ulimit -H -n`
if [ $? -eq 0 ] ; then
  if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
    MAX_FD="$MAX_FD_LIMIT"
  fi

  ulimit -n $MAX_FD
  if [ $? -ne 0 ] ; then
    warn "Could not set maximum file descriptor limit: $MAX_FD"
  fi
else
  warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
fi

# ------------------------------------------------------------------------------
# Trap signals and propagate to the child process.
#
trap 'kill -TERM $PID' TERM INT
$JAVA_EXECUTABLE $SERVER_JAVA_OPTS $USER_JAVA_OPTS $JAVA_ARGS &
PID=$!
wait $PID
trap - TERM INT
wait $PID
EXIT_STATUS=$?