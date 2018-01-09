$serversClusterFolder = "..\BCServersCluster"
$servers = ls ..\BCServersCluster
cd $serversClusterFolder
$path = (Get-Item -Path .\).FullName
$servers | % { 
    cd $path\$_
    Start-Process java -ArgumentList '-jar', '.\target\BCS-1.0-jar-with-dependencies.jar' -RedirectStandardInput $path\$_\commands.txt -RedirectStandardOutput $path\$_\out.txt -RedirectStandardError $path\$_\errors.txt
}
cd $path