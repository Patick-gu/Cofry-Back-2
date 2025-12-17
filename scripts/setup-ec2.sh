#!/bin/bash

set -e

echo "🔧 Configurando servidor EC2 para deploy..."

if ! command -v docker &> /dev/null; then
    echo "📦 Instalando Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    echo "✅ Docker instalado!"
else
    echo "✅ Docker já está instalado"
fi

if ! command -v docker-compose &> /dev/null; then
    echo "📦 Instalando Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose instalado!"
else
    echo "✅ Docker Compose já está instalado"
fi

echo "🔐 Configurando firewall..."
sudo ufw allow 22/tcp
sudo ufw allow 8080/tcp
sudo ufw --force enable

echo "📁 Criando diretório de deploy..."
mkdir -p ~/cofry-backend
cd ~/cofry-backend

echo "✅ Configuração concluída!"
echo ""
echo "📝 Próximos passos:"
echo "1. Copie o arquivo .env.example para .env e configure as variáveis"
echo "2. Configure os secrets no GitHub:"
echo "   - AWS_ACCESS_KEY_ID"
echo "   - AWS_SECRET_ACCESS_KEY"
echo "   - EC2_HOST (IP público ou DNS da instância)"
echo "   - EC2_USER (geralmente 'ubuntu' ou 'ec2-user')"
echo "   - EC2_SSH_PRIVATE_KEY (chave privada SSH da EC2)"
echo "3. Faça push para a branch main/master para iniciar o deploy automático"

