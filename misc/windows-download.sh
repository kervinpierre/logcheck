#!/bin/bash -xv

BKDIR=/cygdrive/c/bk
EXEFILE=logcheck_windows-x64_0_9_0.exe
INSTALL_DIR=/cygdrive/c/Program\ Files/logcheck
EXEUNINSTALL=uninstall.exe

cd "$BKDIR"
rm "$BKDIR/$EXEFILE"

wget http://fastsitesoft.com/logcheck/binaries/$EXEFILE -O $EXEFILE

if [ -e "$INSTALL_DIR/$EXEUNINSTALL" ]; then
    "$INSTALL_DIR/$EXEUNINSTALL"
fi

chmod +x "$BKDIR/$EXEFILE"
"$BKDIR/$EXEFILE"
