# Docker Multi-Environment Setup

## Ambientes Disponíveis

### Development (dev)
```bash
# Usando arquivo .env.dev
docker compose up --build

# Ou especificando o arquivo
docker compose --env-file .env.dev up --build
```

### Production (prod)
```bash
# 1. Crie o arquivo .env.prod a partir do template
cp .env.prod.example .env.prod

# 2. Edite .env.prod com suas credenciais de produção
nano .env.prod

# 3. Execute com as configurações de produção
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up --build
```


### Test (test)
```bash
Ambiente para rodar os testes automatizados com um banco de dados isolado.
# 1. Iniciar o banco de dados de teste
docker compose -f docker-compose.test.yml up -d


# 2. Executar os testes
Os testes estão configurados para usar o perfil 'test' via `@ActiveProfiles("test")`.

./mvnw test


# 3. Parar o banco de dados de teste
docker compose -f docker-compose.test.yml down

```

## Estrutura de Arquivos

```
ExpenseManager/
├── docker-compose.yml              # Configuração base
├── docker-compose.prod.yml         # Overrides para produção
├── docker-compose.test.yml         # Configuração para o banco de dados de teste
├── .env.dev                        # Variáveis de desenvolvimento (gitignore)
├── .env.prod                       # Variáveis de produção (gitignore)
├── .env.prod.example              # Template para produção (versionado)
└── src/
    ├── main/resources/
    │   ├── application.properties      # Configuração base
    │   ├── application-dev.properties  # Configuração de desenvolvimento
    │   └── application-prod.properties # Configuração de produção
    └── test/resources/
        └── application-test.properties # Configuração para testes
```

## Variáveis de Ambiente

### Obrigatórias
- `SPRING_DATASOURCE_URL` - URL do banco de dados
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco

### Opcionais
- `SPRING_PROFILES_ACTIVE` - Perfil ativo (dev/prod)
- `SPRING_FLYWAY_ENABLED` - Habilitar Flyway (default: true)
- `SPRING_FLYWAY_BASELINE_ON_MIGRATE` - Baseline no migrate (default: true)

## Comandos Úteis

```bash
# Ver logs
docker compose logs -f backend

# Parar containers
docker compose down

# Limpar tudo (containers, volumes, imagens)
docker compose down -v --rmi all

# Rebuild sem cache
docker compose build --no-cache

# Verificar health
curl http://localhost:8080/actuator/health
```

## Deploy em Cloud

### Google Cloud Run
```bash
# Build e push
docker build -t gcr.io/PROJECT_ID/expensemanager .
docker push gcr.io/PROJECT_ID/expensemanager

# Deploy
gcloud run deploy expensemanager \
  --image gcr.io/PROJECT_ID/expensemanager \
  --platform managed \
  --region us-central1 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars="SPRING_DATASOURCE_URL=jdbc:postgresql://..." \
  --set-secrets="DB_PASSWORD=db-password:latest"
```

### AWS ECS
```bash
# Build e push para ECR
docker build -t expensemanager .
docker tag expensemanager:latest ACCOUNT.dkr.ecr.REGION.amazonaws.com/expensemanager:latest
docker push ACCOUNT.dkr.ecr.REGION.amazonaws.com/expensemanager:latest
```

### Kubernetes
```bash
# Apply deployment
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
```

## Segurança

⚠️ **NUNCA** commit os seguintes arquivos:
- `.env.dev`
- `.env.prod`
- Qualquer arquivo com credenciais reais

✅ **Sempre** versione:
- `.env.prod.example` (template sem credenciais)
- `docker-compose.yml`
- `docker-compose.prod.yml`
