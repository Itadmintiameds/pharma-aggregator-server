# Loads .env into the current PowerShell session so mvnw/Spring can resolve ${VAR} placeholders.
# Usage: . .\load-env.ps1   (dot-source it so vars persist in your shell)

Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value)
    }
}

# application-test.yml also needs DB_* vars not present in .env; point at local dev Postgres.
$env:DB_URL = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "pharma-aggregator"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "root"

Write-Output "Environment loaded for local build."
