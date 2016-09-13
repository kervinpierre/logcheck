######################################################################################################
##
## Run the LogCheck service in the foreground as a Stand-Alone Java application, and not a server
##
######################################################################################################

$javaHome       = "C:\Program Files\Java\jdk1.8.0_25"
$javaJarPath    = "c:\src\logcheck\src\logcheck\target\logcheck-0.9.jar"
$javaJVM = "$javaHome\bin\java.exe"
$installPath = "C:\Program Files\LC"

& "$javaJVM"  `
    "-Dcom.sun.management.jmxremote.port=3333" `
	"-Dcom.sun.management.jmxremote.ssl=false" `
	"-Dcom.sun.management.jmxremote.authenticate=false" `
	"-Dlog4j.configurationFile=file://c:/program%20files/lc/log4j2.xml" `
	-jar ""$javaJarPath"" `
	        ""--lock-file"" ""c:/windows/temp/logcheck.lck"" `
			""--log-file"" ""c:/logs/webapp/fsastore.com/app.log"" `
			""--elasticsearch-url"" ""http://10.210.66.14:9200/""

