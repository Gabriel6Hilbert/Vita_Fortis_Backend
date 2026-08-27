# Vita Fortis

Aplicacao completa da Vita Fortis: API em Spring Boot e interface React compilada dentro do proprio backend.

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

## Atualizando o frontend incorporado

O build pronto do React fica em `src/main/resources/static`. Caso o projeto frontend seja alterado, execute nele:

```powershell
pnpm install
pnpm build
```

Em seguida, substitua o conteudo de `src/main/resources/static` pelo conteudo gerado na pasta `dist` e valide novamente o backend.

As rotas do React sao encaminhadas para `index.html` pelo `SpaForwardController`, enquanto as rotas iniciadas por `/api` continuam atendidas pelos controllers Spring.

## Perfil de producao

O arquivo `application-prod.properties` aceita configuracao por variaveis de ambiente:

- `PORT`
- `JDBC_URL`
- `DB_USER`
- `DB_PASS`

Exemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:JDBC_URL = "jdbc:mysql://localhost:3306/vitafortis"
$env:DB_USER = "usuario"
$env:DB_PASS = "senha"
.\mvnw.cmd spring-boot:run
```
