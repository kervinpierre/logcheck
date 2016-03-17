#!/bin/bash -xv

cd "${0%/*}"

SCP=scp
RSYNC=rsync

FSSTARGET="/mnt/driveb/kervin/src/fastsitesoft.com/logcheck/svn/src/logcheck/target"
FSSMEDIA="/mnt/driveb/kervin/src/fastsitesoft.com/logcheck/svn/src/logcheck/install/install4j/media/"
SCPHOST="fastsitesoft.com"
REMOTEDIR="/var/www/fastsitesoft.com/www/logcheck/binaries/"

$RSYNC -av $FSSTARGET/*.jar \
           $FSSMEDIA/*.exe  \
           $FSSMEDIA/*.sh   \
           $SCPHOST:$REMOTEDIR --delete

