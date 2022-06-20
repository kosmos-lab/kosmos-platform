#!/usr/bin/env sh
PUID=${PUID:-1000}
PGID=${PGID:-1000}

groupmod -o -g "$PGID" kosmos
usermod -o -u "$PUID" kosmos
echo "UID $(id -u kosmos)  - GID $(id -g kosmos)"
chown -R kosmos:kosmos /app
su -g kosmos kosmos -c "python3 -m pip install --user  -r /app/rules/requirements.txt"
su -g kosmos kosmos -c "java -jar target/kosmos.jar"
