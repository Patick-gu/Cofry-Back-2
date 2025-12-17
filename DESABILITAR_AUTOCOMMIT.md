# 🛑 Autocommit Desabilitado

Os hooks de commit automático foram **desabilitados**.

## O que foi feito:

1. ✅ **`.git/hooks/pre-commit`** - Desabilitado (não executa mais verificações)
2. ✅ **`.git/hooks/post-commit`** - Desabilitado (não executa mais ações)

## Scripts ainda disponíveis (mas não automáticos):

Os scripts de commit manual ainda estão disponíveis caso você queira usá-los manualmente:

- `scripts/auto-commit.ps1` - PowerShell
- `scripts/auto-commit.sh` - Bash
- `scripts/auto-commit-batch.bat` - Windows Batch
- `commit.bat` / `commit.sh` - Atalhos rápidos

## Como fazer commit manual agora:

```bash
git add .
git commit -m "Sua mensagem"
git push origin main
```

## Como reativar o autocommit no futuro:

Se quiser reativar, basta restaurar os hooks originais ou executar os scripts manualmente.

---

**Status:** ✅ Autocommit desabilitado com sucesso!

