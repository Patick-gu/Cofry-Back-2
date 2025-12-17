@echo off
REM Script de Commit Automático - Cofry Backend (Windows Batch)
REM Uso: scripts\auto-commit-batch.bat "Mensagem do commit"

setlocal enabledelayedexpansion

set "MESSAGE=%1"
if "%MESSAGE%"=="" (
    for /f "tokens=2 delims==" %%a in ('wmic os get localdatetime /value') do set datetime=%%a
    set "MESSAGE=Auto commit: !datetime:~0,4!-!datetime:~4,2!-!datetime:~6,2! !datetime:~8,2!:!datetime:~10,2!:!datetime:~12,2!"
)

echo ===========================================
echo   AUTO COMMIT - Cofry Backend
echo ===========================================
echo.

REM Verifica se está em um repositório Git
if not exist .git (
    echo ❌ Erro: Não é um repositório Git!
    exit /b 1
)

REM Verifica se há alterações
echo 📋 Verificando alterações...
git status --porcelain >nul 2>&1
if errorlevel 1 (
    echo ✅ Não há alterações para commitar.
    exit /b 0
)

echo ✅ Alterações encontradas:
git status --short
echo.

REM Adiciona todos os arquivos
echo ➕ Adicionando arquivos ao staging...
git add .

if errorlevel 1 (
    echo ❌ Erro ao adicionar arquivos!
    exit /b 1
)

echo ✅ Arquivos adicionados com sucesso!
echo.

REM Faz o commit
echo 💾 Criando commit...
echo 📝 Mensagem: %MESSAGE%
git commit -m "%MESSAGE%"

if errorlevel 1 (
    echo ❌ Erro ao criar commit!
    exit /b 1
)

echo ✅ Commit criado com sucesso!
echo.

REM Verifica se há remote configurado
git remote get-url origin >nul 2>&1
if not errorlevel 1 (
    echo 🚀 Fazendo push para o repositório remoto...
    git push origin main

    if errorlevel 1 (
        echo ⚠️  Push falhou, mas commit local foi criado.
        echo    Você pode fazer push manualmente depois.
    ) else (
        echo ✅ Push realizado com sucesso!
    )
) else (
    echo ⚠️  Nenhum remote configurado. Apenas commit local criado.
)

echo.
echo ===========================================
echo   ✅ PROCESSO CONCLUÍDO!
echo ===========================================
endlocal

