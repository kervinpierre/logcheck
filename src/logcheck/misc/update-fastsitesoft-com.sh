#!/bin/bash -xv

SCP=scp

FSSTARGET="/mnt/driveb/kervin/src/fastsitesoft.com/logcheck/svn/src/logcheck/target"
FSSMEDIA="/mnt/driveb/kervin/src/fastsitesoft.com/logcheck/svn/src/logcheck/install/install4j/media/"
SCPHOST="fastsitesoft.com"
REMOTEDIR="/var/www/fastsitesoft.com/www/logcheck/binaries/"

$SCP $FSSTARGET/*.jar $SCPHOST:$REMOTEDIR
$SCP $FSSMEDIA/*.exe $SCPHOST:$REMOTEDIR
$SCP $FSSMEDIA/*.sh $SCPHOST:$REMOTEDIR

