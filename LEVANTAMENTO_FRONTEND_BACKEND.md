# Levantamento de requisitos — Frontend x Backend Vita Fortis

Data da análise: 26/08/2026

## Escopo analisado

- Backend: `C:\Users\Usuário\Desktop\vita_fortis\VitaFortis`
- Frontend: `C:\Users\Usuário\Desktop\VitaFortis_Front\vitaFortis`

O frontend é um protótipo estático em HTML, CSS e JavaScript. O backend é uma API Spring Boot com persistência, regras de negócio e 36 operações HTTP distribuídas entre autenticação, produtos, carrinho, pedidos, fidelidade, cupons, usuários administrativos e dados da loja.

## Resumo executivo

No estado atual, frontend e backend ainda não estão integrados. A única tentativa de leitura de dados no frontend é o login/cadastro via `fetch("users.json")`, mas o arquivo existente se chama `db.json`. As demais informações são fixas no HTML, e ações como favoritos, carrinho e assinatura são apenas visuais ou simuladas.

O backend já cobre o núcleo de e-commerce: usuários, catálogo, busca/filtros, estoque, preços/descontos, carrinho, cupom, pedido e fidelidade. Porém, não cobre várias experiências apresentadas no frontend, como assinatura recorrente, favoritos, endereços, cartões, newsletter, candidatura, afiliados, rastreio e conteúdo detalhado do produto.

## Matriz funcional

| Domínio | Frontend | Backend | Situação |
|---|---|---|---|
| Cadastro | Formulário com nome, e-mail, CPF e senha; gravação simulada | `POST /api/v1/auth/cadastro`, persistência e validações | Existe nos dois, falta integrar e alinhar validações |
| Login | E-mail ou CPF, senha e sessão/token fictício | `POST /api/v1/auth/login`, somente e-mail e HTTP Basic | Existe nos dois, contratos incompatíveis |
| Catálogo | Cards e seções fixas | Lista paginada de produtos ativos | Backend pronto; frontend não consome |
| Busca e filtros | Barra, menus de categoria/marca e botões visuais | Busca por texto, nome, descrição, marca, faixa de preço, categoria, resgatável, ordenação e paginação | Backend pronto; frontend ainda sem lógica/API |
| Detalhe do produto | Página fixa com sabor, quantidade, descrição, modo de uso, ingredientes e tabela nutricional | Consulta por ID com dados básicos do produto | Parcial nos dois; faltam campos/variantes no backend e integração no frontend |
| Favoritos | Coração visual e item no perfil | Não existe entidade nem endpoint | Falta no backend e persistência no frontend |
| Carrinho | Ícone e contador visual; sem tela/fluxo funcional | Obter, adicionar, alterar/remover itens, limpar e aplicar/remover cupom | Falta implementar no frontend; resposta do backend precisa totais |
| Cupons | Não há campo/fluxo funcional | Gestão administrativa e aplicação no carrinho/pedido | Falta no frontend |
| Checkout/pedido | Formas de pagamento apenas ilustrativas | Criação de pedido com itens e cupom | Falta checkout no frontend; backend não trata pagamento, endereço ou frete |
| Histórico de pedidos | Link estático em “Minha conta” | Lista de pedidos do usuário | Falta no frontend |
| Status/rastreio | Conteúdo informativo de rastreamento | Status do pedido e alteração pelo admin | Parcial; falta código/eventos de rastreio e telas |
| Fidelidade por pontos | Página institucional, níveis e ofertas fixas | Saldo, meta Diamante, produtos resgatáveis, resgate e histórico | Backend pronto; frontend não consome |
| Assinatura do clube | Planos mensal a anual e modal de seleção | Não existe assinatura/plano/cobrança recorrente | Falta no backend |
| Perfil | Dados fixos, editar, endereços, cartões, pedidos, clube, favoritos, newsletter e sair | Dados do usuário existem; service possui busca/alteração, mas não há controller de perfil | Falta API pública de perfil e integração |
| Administração | Login redireciona admin para `admin.html`, arquivo inexistente | Gestão de produtos, estoque, descontos, cupons, pedidos e usuários | Todo o painel administrativo falta no frontend |
| Informações da loja | Contatos/endereço/redes fixos ou placeholders | `GET /api/v1/loja` | Falta consumo no frontend |
| Fale conosco/FAQ | Conteúdo institucional | Não existe registro de contato/ticket | Se for apenas conteúdo, não exige backend; para envio, falta API |
| Trabalhe conosco | Formulário de candidatura | Não existe | Falta no backend e envio no frontend |
| Afiliados/parceiros | Chamadas e conteúdo | Não existe | Falta no backend se o fluxo for real |
| Políticas | Conteúdo estático | Não existe | Pode permanecer estático/CMS; não é obrigatório no backend |

## O que existe no backend e ainda não existe de forma funcional no frontend

### Prioridade crítica

1. Integração real de cadastro e login.
2. Catálogo dinâmico, paginação, pesquisa, filtros por marca/categoria/preço e ordenação.
3. Detalhe dinâmico do produto por ID.
4. Carrinho completo: leitura, inclusão, atualização, exclusão, limpeza e contador.
5. Aplicação e remoção de cupom.
6. Criação de pedido/checkout e histórico “Meus pedidos”.
7. Saldo, produtos resgatáveis, resgate e histórico de fidelidade.

### Prioridade administrativa

8. Criar `admin.html` ou painel equivalente, pois o login atualmente redireciona para um arquivo inexistente.
9. CRUD administrativo de produtos.
10. Controle de ativação, estoque e descontos de produtos.
11. Gestão de cupons.
12. Lista de pedidos e mudança de status.
13. Lista de usuários, ativação/desativação e mudança de perfil.

### Complementares

14. Carregar telefone, e-mail, Instagram e endereço pelo endpoint da loja.
15. Exibir corretamente estoque, preço original, desconto, preço final e pontuação necessária.

## O que existe no frontend e falta no backend

### Conta do cliente

1. Endpoint autenticado “meu perfil” para consultar e editar os próprios dados.
2. Campos de sobrenome, gênero e data de nascimento, caso devam ser persistidos.
3. Endereços de entrega: CRUD e endereço padrão.
4. Cartões/meios de pagamento salvos, preferencialmente por token de um gateway, sem armazenar número/CVV.
5. Preferência de newsletter.
6. Favoritos: adicionar, remover e listar por usuário.
7. Recuperação/alteração de senha e logout/revogação, caso seja adotado token.

### Comércio e logística

8. Pagamento real: Pix, cartão, boleto etc., com integração a gateway e webhook.
9. Frete, CEP, opções/prazo de entrega e endereço vinculado ao pedido.
10. Rastreamento: transportadora, código e eventos de entrega.
11. Cancelamento/troca/devolução, se as políticas exibidas forem operacionais.
12. Variações de produto, como sabor, peso/tamanho e SKU por variação.
13. Campos estruturados de modo de uso, ingredientes e tabela nutricional.
14. Critérios/coleções para “mais vendidos”, “outlet”, “ofertas”, “esporte” e produtos relacionados.

### Clube e relacionamento

15. Assinatura do Clube Fidelidade: planos, adesão, status, renovação, cancelamento e cobrança recorrente.
16. Alinhar a regra de nível Diamante: o frontend informa 3.000 pontos; o backend usa 5.000 por padrão.
17. Benefícios de assinante, incluindo os 15% anunciados no frontend.
18. Registro de mensagens/solicitações do “Fale conosco”, caso haja formulário real.
19. Candidaturas do “Trabalhe conosco”, incluindo currículo/anexo se necessário.
20. Cadastro e gestão de afiliados/parceiros.

## Incompatibilidades e riscos de integração

1. **Arquivo inexistente no login:** `login.js` busca `users.json`, enquanto o repositório contém `db.json`.
2. **Autenticação incompatível:** o frontend cria `token-fake-*`; o backend não emite token e está configurado com HTTP Basic em modo stateless.
3. **Login por CPF:** o frontend aceita e-mail ou CPF; o DTO do backend valida exclusivamente e-mail.
4. **Senha de cadastro:** o backend exige mínimo de 8 caracteres; o frontend apenas verifica se foi preenchida.
5. **CPF:** o backend valida CPF real; o frontend verifica apenas duplicidade no JSON.
6. **Telefone:** existe no cadastro/backend, mas não é solicitado pelo formulário de cadastro do frontend.
7. **Nome do tipo:** o JSON/frontend usa `tipo`; a resposta da API usa `tipoUsuario`.
8. **Token/autorização:** rotas protegidas não podem ser chamadas usando o token fictício do frontend.
9. **Autorização por proprietário:** rotas de carrinho, pedidos e fidelidade recebem `usuarioId`; é necessário garantir no backend que um cliente não acesse dados de outro usuário.
10. **Carrinho sem totais no contrato:** o serviço calcula subtotal, descontos, total e cupom, mas `CarrinhoResponseDto` expõe apenas IDs e itens.
11. **Conversão carrinho → pedido:** não há endpoint explícito para fechar o carrinho; o frontend teria de remontar os itens em `POST /pedidos` e depois limpar o carrinho.
12. **CORS:** há ativação genérica de CORS, mas não foi localizada uma política explícita de origens permitidas para o frontend.
13. **Meta Diamante divergente:** 3.000 pontos no frontend versus 5.000 no backend.
14. **Categoria com possível erro de grafia:** backend usa `CARBOIDRATROS`, o que tende a quebrar filtros esperados como `CARBOIDRATOS`.
15. **Recursos ausentes:** o frontend referencia oito SVGs de pagamento dentro de `assets/imagens/payments/`, mas essa pasta/arquivos não existem.

## Requisitos recomendados para fechar a integração

### Fase 1 — contrato e autenticação

- Definir o contrato oficial de autenticação: recomenda-se Bearer token/JWT ou sessão segura; remover token fictício e leitura de JSON local.
- Decidir se login por CPF será suportado; alinhar backend e frontend.
- Criar endpoints autenticados `/me` para consultar/editar perfil.
- Aplicar autorização por usuário nas rotas que recebem `usuarioId`.
- Documentar a API com OpenAPI/Swagger e padronizar erros.

### Fase 2 — compra mínima viável

- Consumir catálogo, categorias, filtros e detalhe no frontend.
- Implementar carrinho visual e ajustar o DTO para subtotal, desconto, total e cupom.
- Criar endpoint transacional para fechar carrinho em pedido.
- Implementar histórico/detalhe do pedido.
- Definir pagamento, endereço e frete; se ainda não fizerem parte do MVP, deixar o pedido claramente como reserva/manual.

### Fase 3 — fidelidade

- Integrar saldo, nível, produtos, resgate e histórico.
- Unificar a meta Diamante e as regras de pontuação.
- Separar conceitualmente “programa de pontos” de “assinatura recorrente”.
- Se a assinatura fizer parte do produto, criar domínio próprio de planos e assinaturas.

### Fase 4 — conta e administração

- Endereços, newsletter e favoritos.
- Painel administrativo completo para os endpoints já existentes.
- Depois, cartões tokenizados, rastreamento, candidatura e afiliados conforme prioridade do negócio.

## Critérios mínimos de aceite

- Nenhuma tela usa produtos, usuários ou valores fixos como fonte principal.
- Cadastro e login persistem/autenticam contra a API e exibem erros de validação.
- Usuário autenticado só acessa o próprio carrinho, pedidos, perfil e fidelidade.
- Catálogo e detalhe refletem preço, desconto, estoque e imagem retornados pelo backend.
- Carrinho mantém itens e totais após recarregar a página.
- Pedido é criado de forma atômica, reduz estoque e preserva os preços praticados.
- Painel administrativo exige perfil ADMIN e permite operar os recursos existentes.
- Regras e textos de fidelidade são iguais no frontend e no backend.

## Conclusão

O backend está mais avançado no núcleo transacional, enquanto o frontend está mais avançado na definição visual e na amplitude da experiência desejada. A maior lacuna imediata não é criar mais telas ou mais entidades: é definir o contrato de autenticação, integrar o catálogo/carrinho/pedido já existente e decidir quais promessas visuais do frontend realmente pertencem ao MVP — principalmente assinatura, pagamento, entrega, favoritos e área de conta.
