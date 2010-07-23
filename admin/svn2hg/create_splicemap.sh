#!/bin/bash

set -e

. ./hgutils.sh

add_sm_entry() {
    first="1"
    second="1"
    for desc in "$@"; do
        sha1=`tosha1 "$desc" raw/de.sofd.viskit`
        if [ "`echo -n $sha1 | wc -c`" -ne 40 ]; then
            echo "no valid, unique sha1 found for: $desc" >&2;
            return 1;
        fi
        if [ -z "$first" ]; then  # a " " an the end of the line is evil
            if [ -z "$second" ]; then
                echo -n "," >>splicemap.txt;
            else
                echo -n " " >>splicemap.txt;
                second=""
            fi
        fi
        echo -n "$sha1" >>splicemap.txt
        first=""
    done
    echo >>splicemap.txt
}


rm -f splicemap.txt
touch splicemap.txt


####### create the lines in the splicemap file
##### for add_sm_entry calls with date/time values instead of commit comments for id'ing a commit,
##### the corresponding commit comments are appended in a comment for reference (no comments possible inside multi-line commands)

#####  HieronymusR312046 branch

add_sm_entry \
    'viskit: HieronymusR312046 branch creation' \
    'JGLImageListView: bugfix: more checks for non-null cellsViewer at appropriate places'

add_sm_entry \
    'viskit: HieronymusR312046 changes 740:742 merged into main trunk' \
    'JGLImageListView: bugfix: more checks for non-null cellsViewer at appropriate places' \
    'viskit: bugfix: dispose all sharedContextData attributes when the last GLCanvas that shares the data is disposed'

add_sm_entry \
    'viskit: latest HieronymusR312046 changes merged into main trunk' \
    'viskit: HieronymusR312046 changes 740:742 merged into main trunk' \
    'typo fix: sagital -> sagittal'

add_sm_entry \
    'viskit: merged latest changes from HieronymusR312046 branch (scrollbar fix) into trunk' \
    'viskit: improved visualization of windowing slider #2; start synchronisation of' \
    'viskit: JGLImageListView: fix scrollbar behaviour for empty ListModel'


##### zoompan_transform_refactoring branch

add_sm_entry \
    'viskit: zoompan_transform_refactoring branch started' \
    'viskit: LinAlg convenience'


#####  HieronymusR312043 branch
## TODO: no changes in that branch in svn, thus the branch is not present in hg after this merge (no no-delta branches in hg)

add_sm_entry \
    'viskit: HieronymusR312043 branch started' \
    'viskit: outputGrayscaleRGBs flag parameter removed from texture manager again'



#####  HieronymusDent branch
## TODO: no changes in that branch in svn, thus the branch is not present in hg after this merge (no no-delta branches in hg)

add_sm_entry \
    'viskit: HieronymusDent branch started' \
    'viskit: JOGL utils: + Java2D<->JOGL transformation conversion'


##### StudyBrowser branch

add_sm_entry \
    'viskit: StudyBrowser branch started' \
    'viskit: outputGrayscaleRGBs flag parameter removed from texture manager again'

add_sm_entry \
    '2010-06-14 19:49' \
    'progress bar observer' \
    'ladeoptimierung'
#    'merged latest changes from StudyBrowser branch into trunk'
#    'progress bar observer' \
#    'ladeoptimierung'

add_sm_entry \
    'viskit: merged latest changes from StudyBrowser branch (svn:eol-style prop settings) into trunk' \
    'viskit: coil demo: small enhancement' \
    'viskit: svn propset eol-style native on all java files to aid windoze-linux interop'

add_sm_entry \
    'viskit: merged latest changes from StudyBrowser branch (844:847) into trunk' \
    'viskit: merged latest changes from StudyBrowser branch (svn:eol-style prop settings) into trunk' \
    'transformation bei negativen dicomwerten'

add_sm_entry \
    'viskit: merged latest changes from StudyBrowser branch (848:853) into trunk' \
    '+ LinAlg.vminusv' \
    'behandlung von unsigned 16 bit, speicherung in intbuffer'

add_sm_entry \
    'viskit: merged latest changes from StudyBrowser branch (854:857) into trunk' \
    'LinAlg: make more methods return the destination matrix' \
    'behandlung von unsigned byte auch als shortbuffer (java byte ist signed)'

add_sm_entry \
    'viskit: merged latest changes from trunk (starting after StudyBrowser branch creation) into StudyBrowser branch' \
    'behandlung von unsigned byte auch als shortbuffer (java byte ist signed)' \
    'viskit: CachingDicomImageListModelElement#setUsedPixelValuesRange fixed'

add_sm_entry \
    'viskit: merged latest changes from trunk (LinAlg move to utils) into StudyBrowser branch' \
    'viskit: merged latest changes from trunk (starting after StudyBrowser branch creation) into StudyBrowser branch' \
    'viskit: LinAlg moved to de.sofd.util (package de.sofd.math)'

add_sm_entry \
    'viskit: merged latest changes from StudyBrowser branch into trunk' \
    'viskit: svn2hg splicemap update (async_model_elt_init branch, HieronymusR312043S' \
    'WindowingUtil, verbesserung beim laden von komprimierten bildern, initiales windowing bei komprimierten bildern'


##### async_model_elt_init branch

add_sm_entry \
    'viskit: async_model_elt_init branch created' \
    'viskit: frameCountByDcmObjectIdCacheSize enlarged much'

add_sm_entry \
    'viskit: merged latest changes from trunk into async_model_elt_init branch' \
    'viskit: asynchronous model element initialization: more logging, TODOs, documentation' \
    'viskit: JLutSliderWindowingSlider code changes, ModelFactory calculates pixel data ranges'


##### HieronymusR312043S1 branch

add_sm_entry \
    'branch for HieronymusR312043S1' \
    'build mgmt.'
