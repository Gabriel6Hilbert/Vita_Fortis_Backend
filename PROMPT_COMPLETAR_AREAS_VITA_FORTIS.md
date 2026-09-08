# Prompt para completar as areas do Vita Fortis

Trabalhe no projeto `C:\Users\Usuário\Desktop\vita_fortis\VitaFortis` e implemente as lacunas funcionais do frontend React e do backend Spring Boot. Antes de alterar qualquer arquivo, verifique cuidadosamente o que ja existe em controllers, services, DTOs, entidades, repositorios, seguranca, telas e `frontend/src/services/api.ts`. Reutilize os endpoints e modelos existentes, sem criar fluxos duplicados.

## Objetivo

Transformar as areas de ADMIN, COLABORADOR e CLIENTE em experiencias completas e operacionais, removendo dados demonstrativos das funcoes de negocio e mantendo separacao rigorosa por perfil.

## Regras obrigatorias

- ADMIN deve entrar em `/admin`, COLABORADOR em `/colaborador` e CLIENTE na area comum. ADMIN e COLABORADOR nao devem cair na area do cliente.
- Proteger rotas no frontend e permissoes no backend. Nao confiar apenas em esconder botoes.
- Nunca excluir fisicamente produtos, cupons, usuarios ou dados com historico. Usar ativacao/desativacao e preservar auditoria.
- Usar `BigDecimal` no backend para valores monetarios.
- Nao quebrar os fluxos existentes de catalogo, sacola, checkout, pedido, cashback e autorizacao por proprietario.
- Exibir estados de carregamento, vazio, sucesso e erro em todas as operacoes.
- Depois de cada gravacao, recarregar ou atualizar o estado visivel e mostrar o resultado real retornado pela API.
- Nao apresentar fallback demonstrativo como se fosse dado real.

## ADMIN - completar o painel operacional

1. Produtos e valores:
   - listar ativos e inativos, pesquisar, filtrar e paginar;
   - criar, editar, duplicar, ativar e desativar produto;
   - editar nome, codigo, descricao, marca, unidade/peso, categoria, imagem, preco base, estoque e metadados comerciais;
   - oferecer controles separados para desconto percentual e desconto em valor, usando os endpoints existentes;
   - mostrar preco original, desconto aplicado e preco final antes de salvar;
   - validar valores negativos, desconto maior que o preco, estoque invalido e codigo duplicado;
   - registrar toda movimentacao de estoque com quantidade anterior, quantidade nova, motivo, responsavel e data.

2. Pedidos:
   - listar e filtrar por numero, cliente, periodo, status e pagamento;
   - abrir detalhes completos com itens, cliente, endereco mascarado, retirada/entrega, frete, cupom, subtotal, desconto, total, pagamento e historico de status;
   - aprovar ou recusar pagamento e alterar status somente por transicoes validas;
   - exigir confirmacao e observacao nas operacoes sensiveis;
   - preservar estorno de estoque e cashback quando houver cancelamento.

3. Cupons:
   - criar, editar, ativar e desativar;
   - configurar codigo, descricao, tipo, desconto, subtotal minimo, vencimento, colaborador vinculado e percentual de cashback;
   - mostrar uso, vendas geradas, cashback gerado, validade e situacao;
   - impedir combinacoes invalidas e deixar claro que cashback do colaborador nao e desconto do cliente.

4. Usuarios e colaboradores:
   - listar, pesquisar e filtrar por perfil e status;
   - cadastrar colaborador, alterar perfil, ativar/desativar e conceder permissao de relatorios;
   - criar uma ficha do colaborador com cupons, vendas, saldo, extrato e pedidos vinculados;
   - adicionar ao ADMIN as operacoes existentes de ajuste e baixa de cashback, sempre com valor, justificativa, responsavel e confirmacao.

5. Relatorios e moderacao:
   - criar tela para escolher periodo e baixar os CSVs existentes de pedidos, produtos, clientes, cupons e cashback;
   - mostrar as metricas reais sem substituir falha da API por numeros demonstrativos;
   - criar fila de avaliacoes para aprovar/reprovar usando a API administrativa existente.

## COLABORADOR - criar uma area realmente util

- Manter a area exclusiva e somente com dados do colaborador autenticado.
- Exibir saldo, cashback confirmado, estornado, vendas, pedidos, cupons e extrato auditavel.
- Permitir filtros por periodo, cupom e tipo de movimento.
- Exibir detalhes dos pedidos atribuidos sem revelar dados pessoais desnecessarios do cliente.
- Quando `permissaoRelatorios` estiver ativa, disponibilizar metricas/relatorios autorizados; sem essa permissao, nao renderizar nem permitir a chamada.
- Nao permitir que o colaborador crie produtos, altere precos, altere usuarios, edite cupons ou consulte dados de outro colaborador.

## CLIENTE - substituir a visao crua por uma area completa

- Criar dashboard da conta com saudacao, pedidos recentes, favoritos, enderecos e atalhos claros.
- Permitir editar dados do perfil, preferencias de comunicacao e senha usando os endpoints existentes.
- Criar gerenciamento de enderecos: listar, cadastrar, editar e escolher principal.
- Corrigir favoritos para persistirem na API tambem na pagina de detalhes do produto; remover dependencia de armazenamento local para usuario autenticado.
- Criar formulario de avaliacao para cliente autenticado e mostrar o estado de moderacao quando aplicavel.
- Melhorar acompanhamento de pedido com linha do tempo do historico, detalhes financeiros, entrega/retirada e pagamento.
- Manter o fluxo `Catalogo -> Produto -> Sacola -> Checkout -> Pedido`, com mensagens claras e layout responsivo.
- Substituir campanhas, objetivos, esportes e “mais vendidos” demonstrativos por dados reais/configuraveis ou por estados vazios honestos.

## Pendencias de producao

- Integrar pagamento real e webhook somente depois de definir provedor, credenciais e idempotencia; ate la, identificar claramente o gateway local como simulacao.
- Implementar frete real somente depois de definir regioes, transportadora e regras; ate la, documentar a cotacao fixa.
- Adicionar recuperacao de senha por token e e-mail transacional.
- Remover credenciais do banco de `application.properties`, usar variaveis de ambiente e criar perfis separados para desenvolvimento, teste e producao.
- Adotar migracoes versionadas (Flyway ou Liquibase) e deixar de depender de `ddl-auto=update` em producao.
- Implementar politicas e controles de LGPD/cookies.

## Validacao e criterio de aceite

- Executar `pnpm build` em `frontend` e `mvnw.cmd test` na raiz.
- Criar testes para CRUD/ativacao de produto e cupom, descontos, estoque auditavel, transicoes de pedido, ajuste/baixa de cashback, relatorios e autorizacao entre perfis.
- Validar em navegador, com contas ADMIN, COLABORADOR e CLIENTE, cada botao e cada estado de tela.
- Fazer teste E2E real: criar produto, alterar preco/desconto/estoque, criar cupom vinculado, comprar como cliente, aprovar como ADMIN e conferir cashback no COLABORADOR.
- Provar HTTP 403 para acessos cruzados e operacoes nao autorizadas.
- Distinguir no relatorio final: compilacao, testes automatizados e fluxos E2E realmente executados.
- Entregar uma matriz final por perfil com: requisito, endpoint, tela, teste e status.

Se “OKRs” for um requisito literal do negocio, antes de implementar defina com o responsavel quais objetivos, resultados-chave, periodos, metas, responsaveis e permissoes devem existir. Nao confundir OKRs com pedidos. Se a palavra pretendida era “pedidos”, aplicar a secao de pedidos acima.
