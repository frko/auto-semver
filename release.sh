#!/usr/bin/env bash

# fix 'gpg: signing failed: inappropriate ioctl for device'
GPG_TTY=$(tty)
export GPG_TTY

./mvnw -Possrh clean deploy
