param(
    [Parameter(Mandatory = $true)][string]$DatabaseHost,
    [Parameter(Mandatory = $true)][int]$DatabasePort,
    [Parameter(Mandatory = $true)][string]$DatabaseName,
    [Parameter(Mandatory = $true)][string]$DatabaseUser,
    [Parameter(Mandatory = $true)][string]$OutputDirectory,
    [int]$KeepDaily = 7,
    [int]$KeepWeekly = 4
)

$ErrorActionPreference = 'Stop'
if (-not $env:VITAFORTIS_BACKUP_DB_PASSWORD) { throw 'Defina VITAFORTIS_BACKUP_DB_PASSWORD no ambiente seguro.' }
if ($KeepDaily -lt 2 -or $KeepWeekly -lt 1) { throw 'Preserve ao menos duas cópias diárias e uma semanal.' }
$dumpCommand = Get-Command mysqldump -ErrorAction Stop
$resolvedOutput = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Path $resolvedOutput -Force | Out-Null
$timestamp = (Get-Date).ToUniversalTime().ToString('yyyyMMddTHHmmssZ')
$baseName = "vitafortis-$DatabaseName-$timestamp"
$temporary = Join-Path $resolvedOutput "$baseName.sql.tmp"
$final = Join-Path $resolvedOutput "$baseName.sql"
$manifest = Join-Path $resolvedOutput "$baseName.sha256"
$previousPassword = $env:MYSQL_PWD
try {
    $env:MYSQL_PWD = $env:VITAFORTIS_BACKUP_DB_PASSWORD
    & $dumpCommand.Source --host=$DatabaseHost --port=$DatabasePort --user=$DatabaseUser `
        --single-transaction --quick --hex-blob --routines --triggers --events `
        --default-character-set=utf8mb4 --set-gtid-purged=OFF --result-file=$temporary $DatabaseName
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $temporary)) { throw 'mysqldump não produziu uma cópia válida.' }
    if ((Get-Item -LiteralPath $temporary).Length -eq 0) { throw 'O dump foi gerado vazio.' }
    Move-Item -LiteralPath $temporary -Destination $final
    $hash = Get-FileHash -Algorithm SHA256 -LiteralPath $final
    "$($hash.Hash)  $([System.IO.Path]::GetFileName($final))" | Set-Content -LiteralPath $manifest -Encoding ascii
} finally {
    $env:MYSQL_PWD = $previousPassword
    if (Test-Path -LiteralPath $temporary) { Remove-Item -LiteralPath $temporary }
}
$backups = @(Get-ChildItem -LiteralPath $resolvedOutput -File -Filter "vitafortis-$DatabaseName-*.sql" | Sort-Object LastWriteTimeUtc -Descending)
$expired = @($backups | Select-Object -Skip $KeepDaily)
foreach ($backup in $expired) {
    Remove-Item -LiteralPath $backup.FullName
    $backupManifest = [System.IO.Path]::ChangeExtension($backup.FullName, 'sha256')
    if (Test-Path -LiteralPath $backupManifest) { Remove-Item -LiteralPath $backupManifest }
}
$weeklyDirectory = Join-Path $resolvedOutput 'weekly'
New-Item -ItemType Directory -Path $weeklyDirectory -Force | Out-Null
if ((Get-Date).ToUniversalTime().DayOfWeek -eq [System.DayOfWeek]::Sunday) {
    Copy-Item -LiteralPath $final -Destination (Join-Path $weeklyDirectory ([System.IO.Path]::GetFileName($final)))
    Copy-Item -LiteralPath $manifest -Destination (Join-Path $weeklyDirectory ([System.IO.Path]::GetFileName($manifest)))
}
$weeklyBackups = @(Get-ChildItem -LiteralPath $weeklyDirectory -File -Filter "vitafortis-$DatabaseName-*.sql" | Sort-Object LastWriteTimeUtc -Descending)
foreach ($backup in @($weeklyBackups | Select-Object -Skip $KeepWeekly)) {
    Remove-Item -LiteralPath $backup.FullName
    $backupManifest = [System.IO.Path]::ChangeExtension($backup.FullName, 'sha256')
    if (Test-Path -LiteralPath $backupManifest) { Remove-Item -LiteralPath $backupManifest }
}
Write-Output "Backup concluído: $final"
Write-Output "Manifesto SHA-256: $manifest"
