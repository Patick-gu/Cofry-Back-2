@echo off
REM Atalho rápido para commit no Windows
REM Uso: commit.bat "Mensagem do commit"

if "%1"=="" (
    echo ❌ Por favor, forneça uma mensagem de commit.
    echo Uso: commit.bat "Mensagem do commit"
    exit /b 1
)

powershell -ExecutionPolicy Bypass -File scripts\auto-commit.ps1 "%*"


