$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $repoRoot ".env.local"

if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $repoRoot ".env"
}

if (-not (Test-Path $envFile)) {
    throw "Missing local environment file. Expected .env.local or .env in $repoRoot"
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()

    if ($line -ne "" -and -not $line.StartsWith("#")) {
        $parts = $line -split "=", 2

        if ($parts.Count -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim().Trim('"')
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

Push-Location $repoRoot
try {
    & cmd /c ".\mvnw.cmd spring-boot:run"
}
finally {
    Pop-Location
}
