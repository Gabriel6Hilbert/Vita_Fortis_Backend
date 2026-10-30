param(
    [Parameter(Mandatory = $true)][string]$BackupFile,
    [Parameter(Mandatory = $true)][string]$TargetHost,
    [Parameter(Mandatory = $true)][int]$TargetPort,
    [Parameter(Mandatory = $true)][string]$TargetDatabase,
    [Parameter(Mandatory = $true)][string]$TargetUser,
    [Parameter(Mandatory = $true)][string]$Confirmation
)

$ErrorActionPreference = 'Stop'
if ($TargetDatabase -notmatch '^(restore_|vitafortis_restore_)') { throw 'O banco deve começar com restore_ ou vitafortis_restore_.' }
if ($Confirmation -ne "RESTORE:$TargetDatabase") { throw "Confirmação inválida. Use RESTORE:$TargetDatabase." }
if (-not $env:VITAFORTIS_RESTORE_DB_PASSWORD) { throw 'Defina VITAFORTIS_RESTORE_DB_PASSWORD no ambiente isolado.' }
$resolvedBackup = (Resolve-Path -LiteralPath $BackupFile).Path
$manifest = [System.IO.Path]::ChangeExtension($resolvedBackup, 'sha256')
if (-not (Test-Path -LiteralPath $manifest)) { throw 'Manifesto SHA-256 não encontrado.' }
$expected = ((Get-Content -LiteralPath $manifest -Raw).Trim() -split '\s+')[0]
$actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $resolvedBackup).Hash
if ($expected -ne $actual) { throw 'O hash do backup não corresponde ao manifesto.' }
$mysqlCommand = Get-Command mysql -ErrorAction Stop
$previousPassword = $env:MYSQL_PWD
try {
    $env:MYSQL_PWD = $env:VITAFORTIS_RESTORE_DB_PASSWORD
    & $mysqlCommand.Source --host=$TargetHost --port=$TargetPort --user=$TargetUser `
        --execute="CREATE DATABASE IF NOT EXISTS ``$TargetDatabase`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível preparar o banco isolado.' }
    Get-Content -LiteralPath $resolvedBackup -Raw | & $mysqlCommand.Source `
        --host=$TargetHost --port=$TargetPort --user=$TargetUser $TargetDatabase
    if ($LASTEXITCODE -ne 0) { throw 'A restauração falhou.' }
    & $mysqlCommand.Source --host=$TargetHost --port=$TargetPort --user=$TargetUser $TargetDatabase `
        --table --execute="SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY table_name;"
    if ($LASTEXITCODE -ne 0) { throw 'A verificação das tabelas restauradas falhou.' }
} finally { $env:MYSQL_PWD = $previousPassword }
Write-Output "Restauração verificada no banco isolado: $TargetDatabase"
