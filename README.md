# UBS-ExpenseManager - Grupo-6
Desenvolvimento do projeto para o processo seletivo Trainee UBS 2026.

## Como Executar o Backend

Solicite ao administrador que seu ip seja adicionado aqueles com autorização de acesso ao banco de dados.

Abrir a pasta "ExpenseManager" no terminal.

Adicionar Variáveis de Ambiente:

`
set -a
`

`
source .env
`

`
set +a
`

Opção 1: Executar em ambiente Docker

`
docker compose up  
`

Opção 2: Executar em ambiente Local

`
./mvnw spring-boot:run
`
