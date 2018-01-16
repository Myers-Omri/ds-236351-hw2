$serversClusterFolder = "..\BCServersCluster"
$targetFolder = "target"
$servers = ls ..\BCServersCluster
$servers | % { Copy-Item -Force -Recurse $targetFolder $serversClusterFolder\$_}
$servers | % {Copy-Item -Force $serversClusterFolder\$_\"config.properties" $serversClusterFolder\$_\"target"\"classes"}
$servers | % {Copy-Item -Force $serversClusterFolder\$_\"application.properties" $serversClusterFolder\$_\"target"\"classes"}
