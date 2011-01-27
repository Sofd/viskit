#!/bin/sh

set -e

mkdir -p doc/api
javadoc -d doc/api -sourcepath src `(cd src; find de/ -type d | sed 's/\//./g')`

