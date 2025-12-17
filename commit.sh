#!/bin/bash

# Atalho rápido para commit no Linux/Mac
# Uso: ./commit.sh "Mensagem do commit"

if [ -z "$1" ]; then
    echo "❌ Por favor, forneça uma mensagem de commit."
    echo "Uso: ./commit.sh \"Mensagem do commit\""
    exit 1
fi

bash scripts/auto-commit.sh "$@"

