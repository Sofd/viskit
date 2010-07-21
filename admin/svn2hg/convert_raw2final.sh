#!/bin/sh

set -e

./create_splicemap.sh

hg convert --filemap=filemap.txt --splicemap=splicemap.txt raw/de.sofd.viskit/ de.sofd.viskit

