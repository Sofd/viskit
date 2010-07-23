#!/bin/sh
# pre: hgsubversion extension activated

set -e

mkdir -p raw

hg clone http://192.168.0.123/svn/common/de.sofd.viskit raw/de.sofd.viskit

