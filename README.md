# Vita Fortis

Aplicacao completa da Vita Fortis: API em Spring Boot e interface React compilada dentro do proprio backend. O codigo-fonte React fica em `frontend` e o build servido pelo Spring fica em `src/main/resources/static`.

## Acompanhamento do projeto

Para acompanhar tarefas, arquivos afetados, testes e pendencias pelo computador ou celular, comece pelo [Painel de acompanhamento](PAINEL_ACOMPANHAMENTO.md). Para solicitar uma mudanca pequena e controlada, use o [Modelo de pedido para IA](MODELO_PEDIDO_IA.md).

Ao iniciar o Spring Boot, o site e a API ficam disponíveis no mesmo endereco:

- Site: `http://localhost:5001`
- API: `http://localhost:5001/api/v1`

## Requisitos

- Java 17
- MySQL 8
- Git

O Maven Wrapper ja esta incluido no projeto, portanto nao e necessario instalar o Maven separadamente.

## Banco de dados

Crie um usuario e banco MySQL compatíveis com as configuracoes de `src/main/resources/application.properties`, ou altere estas propriedades para os dados do seu ambiente:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vitafortis
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
```

O parametro `createDatabaseIfNotExist=true` presente na configuracao padrao permite que o banco seja criado automaticamente quando o usuario MySQL tiver permissao.

## Como executar

No Windows (PowerShell ou Prompt de Comando):

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Depois, acesse `http://localhost:5001` no navegador. O React usa a API do mesmo servidor, portanto nao e necessario iniciar outro processo para o frontend.

## Testes e empacotamento

Para executar os testes:

```powershell
.\mvnw.cmd clean test
```

Para gerar o arquivo executavel:

```powershell
.\mvnw.cmd clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Desenvolvimento do frontend

```powershell
cd frontend
pnpm install
pnpm dev
```

O Vite inicia em `http://localhost:5173` e encaminha `/api` para o backend em `http://localhost:5001`.

## Atualizando o frontend incorporado

O build pronto do React fica em `src/main/resources/static`. Caso o projeto frontend seja alterado, execute:

```powershell
cd frontend
pnpm install
pnpm build
```

Em seguida, substitua o conteudo de `src/main/resources/static` pelo conteudo gerado em `frontend/dist` e valide novamente o backend.

## Checkout e integracoes

- Retirada nao cobra frete.
- Entrega exige um endereco cadastrado.
- O frete local usa `vita-fortis.checkout.frete-fixo` e `vita-fortis.checkout.prazo-padrao-dias`.
- `PagamentoLocalGateway` gera uma referencia segura para desenvolvimento. A integracao real com PagBank deve implementar `PagamentoGateway` quando as credenciais forem fornecidas.
- Nenhum numero ou CVV de cartao e armazenado.

## Principais APIs adicionadas

- `GET/POST/PUT/DELETE /api/v1/usuarios/{usuarioId}/enderecos`
- `GET/PUT /api/v1/me`
- `PUT /api/v1/me/senha`
- `GET /api/v1/colaborador/cashback`
- `POST /api/v1/admin/colaboradores/{id}/cashback/ajustes`
- `POST /api/v1/admin/colaboradores/{id}/cashback/baixas`
- `GET /api/v1/admin/relatorios/{tipo}.csv?inicio=AAAA-MM-DD&fim=AAAA-MM-DD`

As rotas do React sao encaminhadas para `index.html` pelo `SpaForwardController`, enquanto as rotas iniciadas por `/api` continuam atendidas pelos controllers Spring.

## Perfil de producao

O arquivo `application-prod.properties` aceita configuracao por variaveis de ambiente:

- `PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Exemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_URL = "jdbc:mysql://localhost:3306/vitafortis"
$env:DB_USERNAME = "usuario"
$env:DB_PASSWORD = "senha"
.\mvnw.cmd spring-boot:run
```

Nao ative o perfil `prod` em um banco existente sem executar o procedimento de inventario,
backup, restauracao isolada e baseline descrito em `docs/operacao/BANCO_BACKUP_FLYWAY.md`.

## E-mail transacional

O projeto usa Spring Mail e esta preparado para o relay SMTP da Brevo. Em producao, cadastre
os valores abaixo como segredos do provedor de hospedagem; nunca grave a chave SMTP no repositorio:

- `MAIL_HOST=smtp-relay.brevo.com`
- `MAIL_PORT=587`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH=true`
- `MAIL_STARTTLS=true`
- `EMAIL_FROM` e `EMAIL_REPLY_TO`
- `FRONTEND_URL`
- `EMAIL_ENABLED=true`, somente depois de verificar remetente, DKIM/DMARC e realizar o teste real

Enquanto `EMAIL_ENABLED=false`, nenhum e-mail e enviado. Falhas de notificacao de pedido sao
registradas depois do commit e nao desfazem pedido, pagamento ou estoque.

## Backup e Flyway

- Procedimento seguro: `docs/operacao/BANCO_BACKUP_FLYWAY.md`.
- Backup diario: `scripts/database/backup-mysql.ps1`.
- Restauracao isolada: `scripts/database/restore-verify-mysql.ps1`.

Os scripts nunca recebem senha como argumento. Use as variaveis temporarias indicadas no runbook.
