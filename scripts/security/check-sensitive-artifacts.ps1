[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$repositoryRoot = git rev-parse --show-toplevel
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($repositoryRoot)) {
    throw 'No se pudo determinar la raiz del repositorio Git.'
}

$blockedExtensions = @('.fbk', '.fdb', '.gbk')
$trackedArtifacts = @(
    git -C $repositoryRoot ls-files |
        Where-Object {
            $extension = [System.IO.Path]::GetExtension($_).ToLowerInvariant()
            $blockedExtensions -contains $extension
        }
)

if ($trackedArtifacts.Count -gt 0) {
    Write-Error (
        "Se detectaron backups o bases Firebird rastreados por Git:`n - " +
        ($trackedArtifacts -join "`n - ") +
        "`nEliminalos del indice y almacenalos en un repositorio privado y cifrado."
    )
    exit 1
}

Write-Output 'OK: Git no rastrea archivos .fbk, .fdb ni .gbk.'
