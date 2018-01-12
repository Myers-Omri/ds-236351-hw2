$serversClusterFolder = "..\BCServersCluster"
$servers = ls ..\BCServersCluster
cd $serversClusterFolder
$path = (Get-Item -Path .\).FullName
for ($i = 0 ; $i -le 30 ; $i++) {
    $servers | % {
        rm $path\$_\outputs\out_$i.txt
    }
}
for ($i = 0 ; $i -le 100 ; $i++) {
    $procs = @()
    $servers | % { 
       cd $path\$_
      $procs += Start-Process java -ArgumentList '-jar', '.\target\BCS-1.0-jar-with-dependencies.jar' -RedirectStandardInput $path\$_\commands.txt -RedirectStandardOutput $path\$_\outputs\out_$i.txt -RedirectStandardError $path\$_\errors.txt -PassThru -NoNewWindow
    }
    $proc_ids = @()
    $procs | % { $proc_ids += $_.Id }
    Wait-Process $proc_ids
    cd $path
    Write-Host "Test [$i] passed" -ForegroundColor Green 
    Start-Sleep -s 5
}