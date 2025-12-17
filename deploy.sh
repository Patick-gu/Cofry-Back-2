#!/bin/bash

set -e

echo "🚀 Iniciando deploy do Cofry Backend..."

IMAGE_NAME="cofry-backend"
IMAGE_TAG="latest"
CONTAINER_NAME="cofry-backend"
WORK_DIR="$HOME/cofry-backend"

cd "$WORK_DIR" || mkdir -p "$WORK_DIR" && cd "$WORK_DIR"

if [ ! -f .env ]; then
    echo "⚠️  Arquivo .env não encontrado. Copiando env.example..."
    if [ -f env.example ]; then
        cp env.example .env
        echo "⚠️  IMPORTANTE: Edite o arquivo .env com suas credenciais antes de continuar!"
        exit 1
    else
        echo "❌ Arquivo env.example não encontrado! Configure manualmente o arquivo .env"
        exit 1
    fi
fi

if [ ! -f image.tar ]; then
    echo "❌ Arquivo image.tar não encontrado!"
    exit 1
fi

echo "📦 Carregando imagem Docker..."
docker load -i image.tar

echo "🛑 Parando container existente (se houver)..."
docker-compose down 2>/dev/null || true

echo "🗑️  Removendo imagens antigas..."
docker rmi $IMAGE_NAME:old 2>/dev/null || true
if docker images | grep -q "$IMAGE_NAME.*$IMAGE_TAG"; then
    docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:old 2>/dev/null || true
fi

echo "🚀 Iniciando novo container..."
docker-compose --env-file .env up -d

echo "⏳ Aguardando container iniciar..."
sleep 15

echo "🔍 Verificando status do container..."
if docker ps | grep -q $CONTAINER_NAME; then
    echo "✅ Deploy concluído com sucesso!"
    echo "📊 Logs do container:"
    docker logs --tail 30 $CONTAINER_NAME
else
    echo "❌ Erro: Container não está rodando!"
    echo "📋 Últimos logs:"
    docker logs --tail 50 $CONTAINER_NAME 2>&1 || true
    exit 1
fi

echo "🧹 Limpando imagens antigas..."
docker rmi $IMAGE_NAME:old 2>/dev/null || true
rm -f image.tar

echo "✨ Deploy finalizado!"

