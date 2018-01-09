$serversClusterFolder = "..\BCServersCluster"
$targetFolder = "target"
$servers = ls ..\BCServersCluster
$servers | % { Copy-Item -Force -Recurse $targetFolder $serversClusterFolder\$_}
$servers | % {Copy-Item -Force $serversClusterFolder\$_\"config.properties" $serversClusterFolder\$_\"target"\"classes"}
