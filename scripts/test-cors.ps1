$ErrorActionPreference = "Stop"

$origin = if ($args.Count -ge 1) { $args[0] } else { "http://localhost:58159" }
$url = if ($args.Count -ge 2) { $args[1] } else { "https://localhost:8080/auth/login" }

[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

$request = [System.Net.WebRequest]::Create($url)
$request.Method = "OPTIONS"
$request.Headers.Add("Origin", $origin)
$request.Headers.Add("Access-Control-Request-Method", "POST")
$request.Headers.Add("Access-Control-Request-Headers", "authorization,content-type")

try {
    $response = $request.GetResponse()
}
catch [System.Net.WebException] {
    $response = $_.Exception.Response

    if ($null -eq $response) {
        throw
    }
}

try {
    Write-Host "Status: $([int]$response.StatusCode) $($response.StatusCode)"
    Write-Host "Access-Control-Allow-Origin: $($response.Headers['Access-Control-Allow-Origin'])"
    Write-Host "Access-Control-Allow-Credentials: $($response.Headers['Access-Control-Allow-Credentials'])"
    Write-Host "Access-Control-Allow-Headers: $($response.Headers['Access-Control-Allow-Headers'])"
    Write-Host "Access-Control-Allow-Methods: $($response.Headers['Access-Control-Allow-Methods'])"
}
finally {
    $response.Close()
}
