#!/bin/bash

# Script de Commit Automático - Cofry Backend
# Uso: ./scripts/auto-commit.sh "Mensagem do commit"

MESSAGE="${1:-Auto commit: $(date '+%Y-%m-%d %H:%M:%S')}"

echo "==========================================="
echo "  AUTO COMMIT - Cofry Backend"
echo "==========================================="
echo ""

# Verifica se está em um repositório Git
if [ ! -d .git ]; then
    echo "❌ Erro: Não é um repositório Git!"
    exit 1
fi

# Verifica se há alterações
echo "📋 Verificando alterações..."
if [ -z "$(git status --porcelain)" ]; then
    echo "✅ Não há alterações para commitar."
    exit 0
fi

echo "✅ Alterações encontradas:"
git status --short
echo ""

# Adiciona todos os arquivos
echo "➕ Adicionando arquivos ao staging..."
git add .

if [ $? -ne 0 ]; then
    echo "❌ Erro ao adicionar arquivos!"
    exit 1
fi

echo "✅ Arquivos adicionados com sucesso!"
echo ""

# Faz o commit
echo "💾 Criando commit..."
echo "📝 Mensagem: $MESSAGE"
git commit -m "$MESSAGE"

if [ $? -ne 0 ]; then
    echo "❌ Erro ao criar commit!"
    exit 1
fi

echo "✅ Commit criado com sucesso!"
echo ""

# Verifica se há remote configurado
if git remote get-url origin > /dev/null 2>&1; then
    echo "🚀 Fazendo push para o repositório remoto..."
    git push origin main

    if [ $? -eq 0 ]; then
        echo "✅ Push realizado com sucesso!"
    else
        echo "⚠️  Push falhou, mas commit local foi criado."
        echo "   Você pode fazer push manualmente depois."
    fi
else
    echo "⚠️  Nenhum remote configurado. Apenas commit local criado."
fi

echo ""
echo "==========================================="
echo "  ✅ PROCESSO CONCLUÍDO!"
echo "==========================================="


