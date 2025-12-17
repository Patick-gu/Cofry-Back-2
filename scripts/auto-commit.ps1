# Script de Commit Automatico - Cofry Backend
# Uso: .\scripts\auto-commit.ps1 "Mensagem do commit"

param(
    [Parameter(Mandatory=$false)]
    [string]$Message = "Auto commit: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
)

$ErrorActionPreference = "Stop"

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  AUTO COMMIT - Cofry Backend" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path .git)) {
    Write-Host "[ERRO] Nao e um repositorio Git!" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Verificando alteracoes..." -ForegroundColor Yellow
$status = git status --porcelain

if ([string]::IsNullOrWhiteSpace($status)) {
    Write-Host "[OK] Nao ha alteracoes para commitar." -ForegroundColor Green
    exit 0
}

Write-Host "[OK] Alteracoes encontradas:" -ForegroundColor Green
git status --short
Write-Host ""

Write-Host "[INFO] Adicionando arquivos ao staging..." -ForegroundColor Yellow
git add .

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Erro ao adicionar arquivos!" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Arquivos adicionados com sucesso!" -ForegroundColor Green
Write-Host ""

Write-Host "[INFO] Criando commit..." -ForegroundColor Yellow
Write-Host "[INFO] Mensagem: $Message" -ForegroundColor Cyan
git commit -m "$Message"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Erro ao criar commit!" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Commit criado com sucesso!" -ForegroundColor Green
Write-Host ""

$remote = git remote get-url origin 2>$null
if ($remote) {
    Write-Host "[INFO] Fazendo push para o repositorio remoto..." -ForegroundColor Yellow
    git push origin main

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Push realizado com sucesso!" -ForegroundColor Green
    } else {
        Write-Host "[AVISO] Push falhou, mas commit local foi criado." -ForegroundColor Yellow
        Write-Host "       Voce pode fazer push manualmente depois." -ForegroundColor Yellow
    }
} else {
    Write-Host "[AVISO] Nenhum remote configurado. Apenas commit local criado." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  [OK] PROCESSO CONCLUIDO!" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Cyan
