#########################################################
##
## Manage the LogCheck service
##
#########################################################

$javaHome       = "C:\Program Files\Java\jdk1.8.0_25"
$procRunExe     = "c:\commons-daemon\amd64\prunsrv.exe"
$javaJarPath    = "c:\src\logcheck\src\logcheck\target\logcheck-0.9.jar"
$javaStartClass = "com.sludev.logs.logcheck.main.LogCheckMain"
$serviceArgs    = """--lock-file""#""c:/windows/temp/logcheck.lck""#""--log-file""#""c:/logs/webapp/fsastore.com/app.log""#""--elasticsearch-url""#""http://10.210.66.14:9200/"""
$javaArgsArray  = New-Object System.Collections.ArrayList
$serviceInstall=$FALSE
$serviceUninstall=$FALSE

$javaJVM = "$javaHome\jre\bin\server\jvm.dll"

foreach ( $el in $args ) 
{
	switch($el)
	{
	    "--service-install" 
		{ 
		    $serviceInstall=$TRUE
		}  
		"--service-run" 
		{ 
		    $serviceRun=$TRUE
		}
		"--service-uninstall" 
		{ 
		    $serviceUninstall=$TRUE
		}
        default
        {
            $res = $javaArgsArray.Add($el)
        }		
	}
}

if( $serviceInstall )
{
	$installStr = "$procRunExe //IS/LOGCHECK  --Classpath=""$javaJarPath"" --DisplayName=""Logcheck"" --StartMode=jvm --StartClass=$javaStartClass --StartMethod=windowsStart  --StopMode=jvm --StopClass=$javaStartClass --StopMethod=windowsStop --JavaHome=""$javaHome"" --Jvm=""$javaJVM"" --StdOutput=auto --StdError=auto --StartParams=$serviceArgs"

	Write-Host "$installStr"
	Invoke-Expression "$installStr"
}

if( $serviceUninstall )
{
	& $procRunExe '//DS/LOGCHECK'
	Write-Host "Service uninstalled"
}

if( $serviceRun )
{
	& $procRunExe "//TS/LOGCHECK "
}

