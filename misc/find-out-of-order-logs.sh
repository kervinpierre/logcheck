#!/bin/bash -xv

while getopts "l:" optionName; do
    case "$optionName" in
        l) LOGFILE="$OPTARG";;
       [?]) echo "Invalid flag";;
    esac
done

if [ -z "$LOGFILE" ]; then
  echo "Please provide a log file with the -l option"
  exit 1
fi

cd "${0%/*}"

GREP=grep

SENDFILTER="putValueObj"

set +x
TSTAMPLIST=`$GREP $SENDFILTER "$LOGFILE" | cut -d"'" -f2`
TSTAMPLISTSORTED=(`echo $TSTAMPLIST | sort`)
TSTAMPLISTSORTEDUNIQUE=(`echo $TSTAMPLIST | sort -u`)

set +v 
echo "Array size is        : '${#TSTAMPLISTSORTED[@]}'"
echo "Array unique size is : '${#TSTAMPLISTSORTEDUNIQUE[@]}'"

echo "Press any key to continue..."
read PROMPT

echo "Comparing..."

TSTAMPCOUNT=0
TSTAMPPREV=""
for TSC in $TSTAMPLIST
do

  if [ "$TSC" != "${TSTAMPLISTSORTEDUNIQUE[$TSTAMPCOUNT]}" ]; then

    echo "Found : '$TSC'\nExpected : '" + ${TSTAMPLISTSORTEDUNIQUE[$TSTAMPCOUNT]} + "'" 
    break

  fi
 
  TSTAMPCOUNT=`expr $TSTAMPCOUNT + 1`

  if (( $TSTAMPCOUNT % 1000  == 0 )); then
     echo "count $TSTAMPCOUNT"
  fi

done 

set -vx
