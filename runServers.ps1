$serversClusterFolder = "..\BCServersCluster"
$servers = ls ..\BCServersCluster
cd $serversClusterFolder
$path = (Get-Item -Path .\).FullName
#for ($i = 0 ; $i -le 0 ; $i++) {
#    $servers | % {
#        rm $path\$_\outputs\out_$i.txt
#    }
#}
for ($i = 0 ; $i -le 0 ; $i++) {
    $procs = @()
    $servers | % { 
       cd $path\$_
      $procs += Start-Process java -ArgumentList '-jar', '.\target\BCS-1.0.jar'  -RedirectStandardOutput $path\$_\out.txt -RedirectStandardError $path\$_\errors.txt
    }
    $proc_ids = @()
    $procs | % { $proc_ids += $_.Id }
    Wait-Process $proc_ids
    cd $path
    Write-Host "Test [$i] passed" -ForegroundColor Green 
    Start-Sleep -s 5
}