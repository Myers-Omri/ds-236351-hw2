 for ($i = 0 ; $i -le 9 ; $i++) {
    $serversClusterFolder = "..\BCServersCluster"
    $servers = ls ..\BCServersCluster
    cd $serversClusterFolder
    $path = (Get-Item -Path .\).FullName
   cd $path
    $servers | % { 
       $s = $_.Name
       $servers | % {
            Compare-Object $(Get-Content $path\$s\outputs\out_$i.txt) $(Get-Content $path\$_\outputs\out_$i.txt) 
       }
    }  
}
    