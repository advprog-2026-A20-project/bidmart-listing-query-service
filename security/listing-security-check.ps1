param(
    [string]$BaseUrl = "http://localhost:8082",
    [string]$ListingId = ""
)

$ErrorActionPreference = "Stop"

function Invoke-Check {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    try {
        & $Action
        Write-Output "PASS,$Name"
    } catch {
        Write-Output "FAIL,$Name,$($_.Exception.Message)"
    }
}

Invoke-Check "health endpoint available" {
    $response = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -UseBasicParsing
    if ($response.StatusCode -ne 200) { throw "Expected 200, got $($response.StatusCode)" }
}

Invoke-Check "public listing endpoint available" {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api/listings?page=0&size=20" -UseBasicParsing
    if ($response.StatusCode -ne 200) { throw "Expected 200, got $($response.StatusCode)" }
}

Invoke-Check "write endpoint rejects unauthenticated caller" {
    try {
        Invoke-WebRequest `
            -Uri "$BaseUrl/api/listings" `
            -Method POST `
            -ContentType "application/json" `
            -Body '{"title":"X","description":"Y","price":1,"category":"OTHER"}' `
            -UseBasicParsing | Out-Null
        throw "Expected 401 or 403, got success"
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        if ($status -ne 401 -and $status -ne 403) {
            throw "Expected 401 or 403, got $status"
        }
    }
}

if ($ListingId) {
    Invoke-Check "listing validation endpoint available" {
        $response = Invoke-WebRequest -Uri "$BaseUrl/api/listings/$ListingId/validation" -UseBasicParsing
        if ($response.StatusCode -ne 200) { throw "Expected 200, got $($response.StatusCode)" }
    }
}

Invoke-Check "no active production secret in tracked files" {
    $matches = rg -n --hidden -i "(BEGIN PRIVATE|BEGIN RSA|api[_-]?key\s*=|password\s*=.+@|jdbc:postgresql://.+:.+@)" `
        -g "!build/**" -g "!.git/**" -g "!gradle/wrapper/gradle-wrapper.jar"
    if ($LASTEXITCODE -eq 0 -and $matches) {
        throw "Potential secret pattern found"
    }
}
