# UBS-ExpenseManager - Grupo-6
Desenvolvimento do projeto para o processo seletivo Trainee UBS 2026.

## Como Executar o Backend

Abrir a pasta "ExpenseManager" no terminal.

Adicionar Variáveis de Ambiente:

```bash
set -a
source .env
set +a
```

Opção 1: Executar em ambiente Docker

```bash
docker compose up  
```

Opção 2: Executar em ambiente Local

```bash
./mvnw spring-boot:run
```