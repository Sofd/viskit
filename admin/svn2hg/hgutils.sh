#!/bin/bash

tosha1() {
    desc="$1"
    wd="$2"
    sha1=`hg log --template '{node} {desc} {date|isodate}\n' "$wd" | grep "$desc" | awk '{print $1}'`
    echo "$sha1"
}
