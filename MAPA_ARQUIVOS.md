# Mapa dos arquivos - Vita Fortis

Use este mapa para começar uma alteração sem abrir o projeto inteiro. A lista indica os primeiros arquivos; dependências adicionais devem ser justificadas antes da edição.

## Regras essenciais

- Fonte React: `frontend/src`.
- Bundle servido pelo Spring: `src/main/resources/static`.
- Nunca editar manualmente arquivos com hash em `src/main/resources/static/assets`.
- API e site são servidos pelo Spring Boot na mesma aplicação.
- Regras de negócio e autorização devem ser garantidas no backend.

## Frontend

### Rotas e estrutura geral

- Rotas: `frontend/src/app/App.tsx`
- Cabeçalho, navegação e rodapé: `frontend/src/components/Layout.tsx`
- Autenticação no navegador: `frontend/src/app/AuthContext.tsx`
- Sacola no navegador: `frontend/src/app/CartContext.tsx`
- Estilos gerais: `frontend/src/styles/global.css`
- Contratos TypeScript: `frontend/src/types/api.ts`
- Chamadas da API: `frontend/src/services/api.ts`

### Páginas

- Home: `frontend/src/pages/HomePage.tsx`
- Catálogo: `frontend/src/pages/CatalogPage.tsx`
- Produto: `frontend/src/pages/ProductPage.tsx`
- Login e recuperação de senha: `frontend/src/pages/AuthPage.tsx`
- Sacola e checkout: `frontend/src/pages/CartPage.tsx`
- Conta, endereços e solicitações LGPD: `frontend/src/pages/AccountPage.tsx`
- Pedidos do cliente: `frontend/src/pages/OrdersPage.tsx`
- Área do colaborador: `frontend/src/pages/CollaboratorPage.tsx`
- Administração: `frontend/src/pages/AdminPage.tsx`
- Páginas institucionais e políticas: `frontend/src/pages/ContentPages.tsx`

### Componentes específicos

- Card de produto: `frontend/src/components/ProductCard.tsx`
- Administração de catálogo: `frontend/src/components/CatalogAdmin.tsx`
- Administração de solicitações LGPD: `frontend/src/components/PrivacyAdmin.tsx`
- Filtros de pedidos: `frontend/src/components/OrderFiltersForm.tsx`
- Estados de carregamento/erro/vazio: `frontend/src/components/States.tsx`

## Backend

Raiz: `src/main/java/VitaFortis/demo`

### Segurança e aplicação

- Permissões e sessão: `config/SecurityConfig.java`
- Encaminhamento das rotas React: `config/SpaForwardController.java`
- Arquivos enviados: `config/UploadResourceConfig.java`
- Configuração comum: `src/main/resources/application.properties`
- Configuração de produção: `src/main/resources/application-prod.properties`
- Implantação no Render: `render.yaml` e `Dockerfile`

### Autenticação, usuário e recuperação de senha

- `v1/controller/AuthController.java`
- `v1/controller/PerfilController.java`
- `v1/controller/UsuarioAdminController.java`
- `v1/service/UsuarioService.java`
- `v1/service/RecuperacaoSenhaService.java`
- Entidades, DTOs e repositories com o mesmo domínio.

### Produtos e catálogo

- `v1/controller/ProdutoController.java`
- `v1/controller/ProdutoAdminController.java`
- `v1/controller/CatalogoCadastroController.java`
- `v1/service/ProdutoService.java`
- `v1/service/CatalogoCadastroService.java`
- `v1/repository/ProdutoRepository.java`
- `src/main/resources/catalogo-ftw.json`

### Sacola, pedido, pagamento, estoque e frete

- `v1/controller/CarrinhoController.java`
- `v1/controller/PedidoController.java`
- `v1/controller/PedidoAdminController.java`
- `v1/service/CarrinhoService.java`
- `v1/service/PedidoService.java`
- `v1/service/FreteService.java`
- `v1/integration/PagamentoGateway.java`
- `v1/integration/PagamentoLocalGateway.java`

### Cupons e cashback

- `v1/controller/CupomAdminController.java`
- `v1/controller/CashbackController.java`
- `v1/controller/CashbackAdminController.java`
- `v1/service/CupomService.java`
- `v1/service/CupomRegrasService.java`
- `v1/service/CashbackService.java`

### LGPD e privacidade

- Frontend do cliente: `frontend/src/pages/AccountPage.tsx`
- Frontend administrativo: `frontend/src/components/PrivacyAdmin.tsx`
- Contrato HTTP: `frontend/src/services/api.ts` e `frontend/src/types/api.ts`
- `v1/controller/PrivacidadeController.java`
- `v1/controller/PrivacidadeAdminController.java`
- `v1/service/PrivacidadeService.java`
- `v1/entity/SolicitacaoPrivacidade.java`
- `v1/entity/AuditoriaSolicitacaoPrivacidade.java`
- `v1/repository/SolicitacaoPrivacidadeRepository.java`
- `v1/repository/AuditoriaSolicitacaoPrivacidadeRepository.java`
- `src/main/resources/db/migration/V2__cria_solicitacoes_privacidade_rcp02.sql`

### Métricas e relatórios

- `v1/controller/MetricasAdminController.java`
- `v1/controller/RelatorioAdminController.java`
- `v1/service/MetricasService.java`
- `v1/service/RelatorioService.java`

## Onde procurar testes

- Backend: `src/test/java/VitaFortis/demo`
- Migrações: testes com nome relacionado a `Migration` ou requisito correspondente.
- O frontend ainda não possui uma suíte automatizada de testes identificada; lint e build não substituem testes de comportamento.

## Arquivos gerados e temporários

- `frontend/dist`: saída do build, ignorada pelo Git.
- `src/main/resources/static/assets`: bundle incorporado ao Spring; revisar antes de versionar.
- `target`: saída Maven.
- `tmp`: arquivos temporários.
- `output`: relatórios e entregáveis; só versionar quando fizerem parte da entrega.

