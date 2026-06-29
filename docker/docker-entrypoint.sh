#!/bin/sh
# Docker entrypoint for izgw-transform (SQL-enabled build).
# Usage:
#   docker run <image>                 -- starts the transform service (default)
#   docker run <image> generate-token  -- prints signed JWTs and exits

if [ "$1" = "generate-token" ]; then
    exec node /usr/share/izg-transform/generate-token.js
fi

exec sh -c "crond && exec bash run.sh app.jar"
