$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $repoRoot ".env.local"

if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $repoRoot ".env"
}

if (-not (Test-Path $envFile)) {
    throw "Missing local environment file. Expected .env.local or .env in $repoRoot"
}

$localEnv = @{}
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    if ($line -ne "" -and -not $line.StartsWith("#")) {
        $parts = $line -split "=", 2

        if ($parts.Count -eq 2) {
            $localEnv[$parts[0].Trim()] = $parts[1].Trim().Trim('"')
        }
    }
}

$rabbitUser = $localEnv["RABBITMQ_USERNAME"]
$rabbitPassword = $localEnv["RABBITMQ_PASSWORD"]

if ([string]::IsNullOrWhiteSpace($rabbitUser) -or [string]::IsNullOrWhiteSpace($rabbitPassword)) {
    throw "RABBITMQ_USERNAME and RABBITMQ_PASSWORD must be set in $envFile"
}

Push-Location $repoRoot
try {
    & docker compose up -d mysql rabbitmq
    $dockerComposeExitCode = $LASTEXITCODE

    if ($dockerComposeExitCode -ne 0) {
        Write-Warning "Docker Compose could not start the local services. If MySQL and RabbitMQ are already running, the script will still try to repair RabbitMQ access."
    }

    $managementUrl = "http://localhost:15672"
    $authCandidates = @(
        @{ Username = $rabbitUser; Password = $rabbitPassword },
        @{ Username = "guest"; Password = "guest" }
    )

    $auth = $null
    $deadline = (Get-Date).AddMinutes(2)

    while ((Get-Date) -lt $deadline -and $null -eq $auth) {
        foreach ($candidate in $authCandidates) {
            $pair = "{0}:{1}" -f $candidate.Username, $candidate.Password
            $encoded = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($pair))
            $headers = @{ Authorization = "Basic $encoded" }

            try {
                Invoke-RestMethod -Method Get -Uri "$managementUrl/api/overview" -Headers $headers | Out-Null
                $auth = @{
                    Headers = $headers
                    Username = $candidate.Username
                }
                break
            }
            catch {
                Start-Sleep -Seconds 2
            }
        }
    }

    if ($null -eq $auth) {
        throw "Could not authenticate to RabbitMQ management at $managementUrl with either $rabbitUser/$rabbitPassword or guest/guest."
    }

    $userBody = @{
        password = $rabbitPassword
        tags = "administrator"
    } | ConvertTo-Json

    $permissionBody = @{
        configure = ".*"
        write = ".*"
        read = ".*"
    } | ConvertTo-Json

    Invoke-RestMethod -Method Put -Uri "$managementUrl/api/users/$rabbitUser" -Headers $auth.Headers -ContentType "application/json" -Body $userBody | Out-Null
    Invoke-RestMethod -Method Put -Uri "$managementUrl/api/permissions/%2F/$rabbitUser" -Headers $auth.Headers -ContentType "application/json" -Body $permissionBody | Out-Null

    if ($dockerComposeExitCode -eq 0) {
        Write-Host "Local MySQL and RabbitMQ are running."
    }

    Write-Host "RabbitMQ user '$rabbitUser' is ready."
}
finally {
    Pop-Location
}
