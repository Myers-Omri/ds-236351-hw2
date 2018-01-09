for ($i = 0 ; $i -le 9 ; $i++) {
    $serversClusterFolder = "..\BCServersCluster"
    $servers = ls ..\BCServersCluster
    cd $serversClusterFolder
    $path = (Get-Item -Path .\).FullName
    $procs = @()
    $servers | % { 
       cd $path\$_
      $procs += Start-Process java -ArgumentList '-jar', '.\target\BCS-1.0-jar-with-dependencies.jar' -RedirectStandardInput $path\$_\commands.txt -RedirectStandardOutput $path\$_\outputs\out_$i.txt -RedirectStandardError $path\$_\errors.txt -PassThru
    }
    $proc_ids = @()
    $procs | % { $proc_ids += $_.Id }
    Wait-Process $proc_ids
    Start-Sleep -Seconds 5
    cd $path
}