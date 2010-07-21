#!/bin/sh
# pre: hgsubversion installed

set -e

./convert_svn2raw.sh
./convert_raw2final.sh
