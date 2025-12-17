# Script para remover BOM de arquivos Java
# Uso: .\scripts\remove-bom.ps1

$ErrorActionPreference = "Continue"

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  REMOVER BOM DE ARQUIVOS JAVA" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse
$fixedCount = 0
$totalCount = $javaFiles.Count

Write-Host "[INFO] Encontrados $totalCount arquivos Java para verificar..." -ForegroundColor Yellow
Write-Host ""

foreach ($file in $javaFiles) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        
        # Verifica se tem BOM (UTF-8 BOM = 0xEF, 0xBB, 0xBF)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            Write-Host "[FIX] Removendo BOM de: $($file.FullName)" -ForegroundColor Yellow
            
            # Remove os primeiros 3 bytes (BOM)
            $contentWithoutBom = $bytes[3..($bytes.Length - 1)]
            
            # Salva sem BOM usando UTF-8
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $utf8NoBom.GetString($contentWithoutBom), $utf8NoBom)
            
            $fixedCount++
        }
    } catch {
        Write-Host "[ERRO] Erro ao processar $($file.FullName): $_" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  RESULTADO" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "[OK] Arquivos verificados: $totalCount" -ForegroundColor Green
Write-Host "[OK] Arquivos corrigidos: $fixedCount" -ForegroundColor Green
Write-Host ""

if ($fixedCount -gt 0) {
    Write-Host "[INFO] BOM removido de $fixedCount arquivo(s)." -ForegroundColor Yellow
} else {
    Write-Host "[OK] Nenhum arquivo com BOM encontrado." -ForegroundColor Green
}

Write-Host ""

