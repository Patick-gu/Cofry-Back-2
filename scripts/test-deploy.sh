#!/bin/bash

set -e

echo "🧪 Testando configuração de deploy..."

ERRORS=0

check_command() {
    if command -v $1 &> /dev/null; then
        echo "✅ $1 instalado"
        return 0
    else
        echo "❌ $1 não encontrado"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo "✅ $1 existe"
        return 0
    else
        echo "❌ $1 não encontrado"
        ERRORS=$((ERRORS + 1))
        return 1
    fi
}

echo "📋 Verificando comandos necessários..."
check_command docker
check_command docker-compose
check_command mvn
check_command java

echo ""
echo "📁 Verificando arquivos necessários..."
check_file Dockerfile
check_file docker-compose.yml
check_file pom.xml
check_file deploy.sh
check_file env.example

echo ""
echo "🔍 Verificando estrutura do projeto..."
if [ -d "src/main/java" ]; then
    echo "✅ Estrutura do projeto Java OK"
else
    echo "❌ Estrutura do projeto Java não encontrada"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "🐳 Testando build Docker..."
if docker build -t cofry-backend:test . > /dev/null 2>&1; then
    echo "✅ Build Docker bem-sucedido"
    docker rmi cofry-backend:test > /dev/null 2>&1
else
    echo "❌ Build Docker falhou"
    ERRORS=$((ERRORS + 1))
fi

echo ""
if [ $ERRORS -eq 0 ]; then
    echo "✨ Todas as verificações passaram!"
    exit 0
else
    echo "⚠️  Encontrados $ERRORS erro(s). Corrija antes de fazer deploy."
    exit 1
fi

