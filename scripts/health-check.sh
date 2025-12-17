#!/bin/bash

API_URL="${1:-http://localhost:8080}"
TIMEOUT=30

echo "🏥 Verificando saúde da API em $API_URL..."

for i in {1..6}; do
    if curl -f -s "$API_URL/api/users" > /dev/null 2>&1; then
        echo "✅ API está respondendo corretamente!"
        exit 0
    fi
    
    echo "⏳ Aguardando API iniciar... ($i/6)"
    sleep 5
done

echo "❌ API não está respondendo após $TIMEOUT segundos"
exit 1

