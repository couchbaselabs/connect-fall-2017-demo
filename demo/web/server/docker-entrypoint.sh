#! /bin/bash

set -e

if [ "$1" = 'cbhealth' ]; then
    exec node "$@"
fi

exec "$@"