# 🤖 Guia de Commit Automático

Sistema automatizado para fazer commits e push de alterações no repositório.

## 📋 Métodos Disponíveis

### 1. Script PowerShell (Windows - Recomendado)

```powershell
# Commit com mensagem personalizada
.\scripts\auto-commit.ps1 "Minha mensagem de commit"

# Commit automático (usa timestamp)
.\scripts\auto-commit.ps1
```

### 2. Script Bash (Linux/Mac/Git Bash)

```bash
# Dar permissão de execução (primeira vez)
chmod +x scripts/auto-commit.sh

# Commit com mensagem personalizada
./scripts/auto-commit.sh "Minha mensagem de commit"

# Commit automático (usa timestamp)
./scripts/auto-commit.sh
```

### 3. Script Batch (Windows CMD)

```cmd
REM Commit com mensagem personalizada
scripts\auto-commit-batch.bat "Minha mensagem de commit"

REM Commit automático (usa timestamp)
scripts\auto-commit-batch.bat
```

### 4. Makefile (Todas as Plataformas)

```bash
# Commit com mensagem personalizada
make commit MESSAGE="Minha mensagem de commit"

# Commit automático (usa timestamp)
make commit
```

## 🎯 O que os Scripts Fazem

1. ✅ **Verificam** se há alterações para commitar
2. ✅ **Adicionam** todos os arquivos ao staging (`git add .`)
3. ✅ **Criam** o commit com a mensagem fornecida
4. ✅ **Fazem push** automaticamente para `origin main` (se configurado)

## 🔧 Git Hooks (Automação Avançada)

### Pre-commit Hook
Executa verificações **antes** de permitir o commit:
- ✅ Detecta arquivos grandes (>10MB)
- ✅ Alerta sobre possíveis senhas hardcoded

### Post-commit Hook
Executa ações **após** commit bem-sucedido:
- ✅ Mostra informações do commit
- ✅ Pergunta se deseja fazer push automaticamente

**Para ativar os hooks:**
```bash
# Linux/Mac/Git Bash
chmod +x .git/hooks/pre-commit
chmod +x .git/hooks/post-commit

# Windows (Git Bash)
chmod +x .git/hooks/pre-commit
chmod +x .git/hooks/post-commit
```

## 📝 Exemplos de Uso

### Commit simples
```powershell
.\scripts\auto-commit.ps1 "Atualização de configurações do banco"
```

### Commit automático com timestamp
```powershell
.\scripts\auto-commit.ps1
# Resultado: "Auto commit: 2025-01-16 14:30:45"
```

### Commit via Makefile
```bash
make commit MESSAGE="Correção de bug na autenticação"
```

## ⚙️ Configuração

### Windows PowerShell
Se você receber erro de política de execução:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Linux/Mac
Certifique-se de que os scripts têm permissão de execução:
```bash
chmod +x scripts/*.sh
```

## 🔍 Verificação Manual

Se você quiser verificar o que será commitado antes:
```bash
git status
git diff --staged
```

## 🚨 Troubleshooting

### Erro: "Não é um repositório Git"
- Certifique-se de estar na raiz do projeto

### Erro: "Nenhum remote configurado"
- Configure o remote: `git remote add origin <URL>`

### Erro: "Push falhou"
- Verifique sua conexão com a internet
- Verifique suas credenciais Git
- Faça push manualmente: `git push origin main`

### Hook não executa
- Verifique permissões: `chmod +x .git/hooks/*`
- Verifique se os arquivos têm `#!/bin/bash` na primeira linha

## 💡 Dicas

1. **Mensagens Descritivas**: Use mensagens claras sobre o que foi alterado
2. **Commits Frequentes**: Faça commits pequenos e frequentes
3. **Review Antes**: Sempre revise as alterações antes de commitar
4. **Backup**: Os scripts não fazem backup, certifique-se de ter tudo salvo

## 📚 Recursos Adicionais

- [Documentação Git](https://git-scm.com/docs)
- [Conventional Commits](https://www.conventionalcommits.org/)

---

**Status:** ✅ Sistema de commit automático configurado e pronto para uso!


