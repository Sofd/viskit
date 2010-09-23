#!/bin/sh
# pre: hgsubversion installed

set -e

#./convert_svn2raw.sh
#./convert_raw2final.sh


#remove wrongly created ui-lib-neutrality-refactoring-pp branch

. ./hgutils.sh

sha1="`tosha1 'viskit: ui-lib-neutrality-refactoring-pp branch created' de.sofd.viskit/`"
if [ "`echo -n $sha1 | wc -c`" -ne 40 ]; then
    echo "no unique sha1 found for the wrongly created ui-lib-neutrality-refactoring-pp svn branch??"
else
    echo "removing wrongly created ui-lib-neutrality-refactoring-pp svn branch"
    (set -e; cd de.sofd.viskit; hg strip "$sha1")
fi
