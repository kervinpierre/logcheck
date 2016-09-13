$JAVA_CMD=(Get-Command java).Path
$TEMP_DIR="c:/windows/temp"
$TARGET_DIR="c:/src/sludev.com/logcheck/logcheck-sample-app/target"
$JAR_FILE="logcheck-sample-app-0.9.jar"

cd $TARGET_DIR

$callArgs = @( '-jar', $JAR_FILE, '--output-file', "$TEMP_DIR/logcheck-sample-app-output1.txt", '--delete-logs', '--output-frequency', '50ms', '--stop-after-count', '1000K' )
$currCmd = $JAVA_CMD
try
{
    Start-Process -WorkingDirectory $TARGET_DIR -NoNewWindow -FilePath $currCmd -ArgumentList $callArgs   
}
catch
{
    Write-Host "Error: '$currCmd $CallArgs' return $LastExitCode "
    exit 1
}

$callArgs = @( '-jar', $JAR_FILE, '--output-file', "$TEMP_DIR/logcheck-sample-app-output2.txt", '--delete-logs', '--output-frequency', '50ms', '--stop-after-count', '1000K' )
$currCmd = $JAVA_CMD
try
{
    Start-Process -WorkingDirectory $TARGET_DIR -NoNewWindow -FilePath $currCmd -ArgumentList $callArgs   
}
catch
{
    Write-Host "Error: '$currCmd $CallArgs' return $LastExitCode "
    exit 1
}

#Remove-EventLog -LogName LogCheckTestAA
New-EventLog    -LogName LogCheckTestAA -Source logchecksrc
Limit-EventLog  -LogName LogCheckTestAA -OverflowAction OverWriteAsNeeded -MaximumSize 1MB 

for( $i = 1
     $i -le 1000
     $i++ )
{
	$currMsg  = "Message is " + [guid]::NewGuid().ToString().ToUpper() + ".`n"
	$currMsg += "Next line is " + [guid]::NewGuid().ToString().ToUpper() + ".`n"
	
	$currEventType = Get-Random -input "Error", "FailureAudit", "Information", "SuccessAudit", "Warning"

	Write-EventLog -LogName LogCheckTestAA -Source logchecksrc -Message $currMsg -EventId 0 -EntryType $currEventType
	
	Write-Host "Wrote Event $i"
	
	Start-Sleep -s 2
}

Write-Host "\n$(Split-Path $MyInvocation.PSCommandPath -Leaf) Completed`n"

