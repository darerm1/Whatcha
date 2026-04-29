param(
    [string]$BaseUrl = "https://alfaitmo.ru",
    [string]$Namespace = "com.darerm1.whatcha/lab7/server-sdui/v1",
    [string]$ConfigDir = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ConfigDir)) {
    $candidateDirs = @(
        $PSScriptRoot,
        (Join-Path (Split-Path -Parent $PSScriptRoot) "docs\bdui"),
        (Split-Path -Parent $PSScriptRoot)
    )

    $ConfigDir = $candidateDirs | Where-Object {
        Test-Path -LiteralPath (Join-Path $_ "manifest.json")
    } | Select-Object -First 1
}

if ([string]::IsNullOrWhiteSpace($ConfigDir)) {
    throw "Unable to resolve config directory. Pass -ConfigDir explicitly."
}

$payloads = @(
    @{ File = "manifest.json"; RelativePath = "$Namespace/manifest" },
    @{ File = "detail-fragment.json"; RelativePath = "$Namespace/fragments/detail" }
)

foreach ($payload in $payloads) {
    $filePath = Join-Path $ConfigDir $payload.File
    if (-not (Test-Path -LiteralPath $filePath)) {
        throw "Missing payload file: $filePath"
    }

    $json = Get-Content -LiteralPath $filePath -Raw
    $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $encodedPath = [System.Uri]::EscapeDataString($payload.RelativePath)
    $url = "$BaseUrl/server/echo/$encodedPath"

    Write-Host "Uploading $($payload.File) -> $($payload.RelativePath)"
    Invoke-RestMethod `
        -Uri $url `
        -Method Put `
        -ContentType "application/json; charset=utf-8" `
        -Body $bodyBytes | Out-Null

    $verification = Invoke-RestMethod `
        -Uri $url `
        -Method Get |
        ConvertTo-Json -Depth 20

    Write-Host "Published: $url"
    Write-Host "Verified GET after PUT"
    Write-Host "Verified payload bytes: $($verification.Length)"
    Write-Host ""
}
