# ✅ Checklist de Deploy - Cofry Backend

## Pré-Deploy

### AWS Setup
- [ ] Instância EC2 criada (Ubuntu 22.04 LTS)
- [ ] Security Group configurado (portas 22 e 8080 abertas)
- [ ] Elastic IP associado (opcional)
- [ ] Chave SSH (.pem) baixada e protegida
- [ ] RDS PostgreSQL criado e acessível
- [ ] Security Group do RDS permite conexões da EC2

### GitHub Setup
- [ ] Repositório criado
- [ ] Código pushado para `main` ou `master`
- [ ] Secrets configurados:
  - [ ] `AWS_ACCESS_KEY_ID`
  - [ ] `AWS_SECRET_ACCESS_KEY`
  - [ ] `EC2_HOST`
  - [ ] `EC2_USER`
  - [ ] `EC2_SSH_PRIVATE_KEY`

## Deploy Inicial

### No EC2
- [ ] Conectado via SSH
- [ ] Docker instalado
- [ ] Docker Compose instalado
- [ ] Diretório `~/cofry-backend` criado
- [ ] Arquivo `.env` configurado com credenciais corretas

### Primeiro Deploy
- [ ] Executado `setup-ec2.sh`
- [ ] `.env` criado a partir de `env.example`
- [ ] Credenciais do banco configuradas no `.env`
- [ ] Deploy inicial executado (manual ou via GitHub Actions)

## Verificação

### Testes Básicos
- [ ] Container está rodando: `docker ps`
- [ ] Logs sem erros: `docker logs cofry-backend`
- [ ] API responde: `curl http://localhost:8080/api/users`
- [ ] Health check passa: `./scripts/health-check.sh`

### Testes de Integração
- [ ] Teste de login: `POST /api/auth/login`
- [ ] Teste de criação de usuário: `POST /api/users`
- [ ] Conexão com banco funcionando

## Deploy Automático

### GitHub Actions
- [ ] Workflow `.github/workflows/deploy.yml` existe
- [ ] Secrets configurados corretamente
- [ ] Push para `main` trigger deploy
- [ ] Deploy automático funcionando

## Pós-Deploy

### Monitoramento
- [ ] Logs sendo monitorados
- [ ] Uso de recursos verificado
- [ ] Alertas configurados (opcional)

### Documentação
- [ ] README atualizado
- [ ] DEPLOY.md revisado
- [ ] Credenciais seguras (não commitadas)

## Troubleshooting

### Se algo der errado:
1. Verificar logs: `docker logs cofry-backend`
2. Verificar variáveis: `docker exec cofry-backend env`
3. Verificar conectividade: `docker exec cofry-backend ping rds-endpoint`
4. Reiniciar container: `docker restart cofry-backend`

---

**Última atualização:** $(date)

