# Parecer de homologação funcional — VitaFortis, 05/11/2026

**Liberação integral não recomendada.** A cobertura desta rodada é parcial. O fluxo de relatórios do colaborador não está disponível no candidato, mesmo com a permissão ativada. O pacote local corrigido precisa ser identificado por um commit antes de seguir para o ambiente público.

## Versões e método

| Ambiente | Versão e evidência |
| --- | --- |
| Render, leitura em 05/11 | HTTP 200 em `/` e `GET /api/v1/loja/versao`. SHA `0ac066878cd3d2b896b499cc0392de6c30d7432f`, `index-BF-mC3lG.js`, `index-52AEm6TZ.css`, pagamento `SIMULADO`. Os dois hashes servidos coincidem com os blobs do commit. |
| Candidato inicial | Branch `qa/vitafortis-2026-11-05`, HEAD `e2374dde8c2fc5b333ba5c96d4236dd32a91c8a6`, `index-CUz5ko25.js`, `index-6Q_CP6RN.css`. JAR inicial SHA-256 `73dbb76164a13a6ae91e899de4c0a988da13be55307e7405a5f4f6824d878b20`. |
| Reteste intermediário | Árvore local sobre o mesmo HEAD, com as alterações listadas neste relatório. Bundles `index-Ccit84-G.js` e `index-1gER0PjL.css`. JAR SHA-256 `bc45280f30ea4aec55a991f4684bc4945d657afb408732222e8c71c117005870`. |
| Reteste da sacola | Mesma base Git e mesmo Java, mais a correção dos controles da sacola móvel. Bundles `index-D0-H-88l.js` e `index-C9mIINJx.css`. JAR SHA-256 `3235ce1a1e46a7d62a2deb8d2a02eba3778c39cdaf7f88041c2cf2455df88d51`. |
| Reteste final | Mesma base Git e mesmo Java, com ajuste na transição pós-recuperação. Bundles `index-BVBiYByX.js` e `index-C9mIINJx.css`. JAR SHA-256 `473d014d9f0027b67193298c0837e10f67cb3927d2bfc26fccad2ae0e68e985b`. |
| Isolamento | Spring em `127.0.0.1:5002` e `127.0.0.1:5003`, MySQL 8.0.44 em `127.0.0.1:33419`, schema dedicado `vitafortis_qa_v4`. Dados fictícios, contextos Edge/Playwright separados por perfil e conta. Desktop 1440 × 900 e celular **simulado por viewport** 390 × 844. O reteste final usa SMTP de captura em `127.0.0.1:5025`. Rate limit local de 100 tentativas por janela. |

Inventário derivado da [matriz canônica](../STATUS_CONSOLIDADO_V2.md), rotas React, menu, controllers e serviços. **Passou** refere-se ao cenário descrito na linha. Evidências da rodada anterior são rotuladas separadamente. Nesta rodada, os testes gerais foram executados no pacote intermediário e os fluxos críticos foram repetidos após a última alteração de CSS. Uma negativa por API é apoio para a regra de acesso.

## CLIENTE

| Função e limite | Estado | Execução e evidência |
| --- | --- | --- |
| Cadastro e indisponibilidade | Passou | Simulação de HTTP 503 preservou o e-mail e exibiu aviso. Após restabelecer a API, o cadastro e login funcionaram. CPF inválido e duplicidade foram cobertos na rodada anterior. [503](../output/qa-e2374dd/cadastro-503.png) · [Cadastro](../output/qa-e2374dd/cadastro-final.png) |
| Login e logout | Passou | Login com contas fictícias em contextos distintos. Após sair e abrir Minha conta, a tela pediu login. [Logout](../output/qa-e2374dd/logout.png) |
| Recuperação de senha, solicitação | Passou | Tela de recuperação enviou a solicitação e mostrou resposta genérica. [Captura](../output/qa-e2374dd/recuperacao-solicitada.png) |
| Recuperação com link e novo login | Passou | SMTP em loopback capturou o e-mail fictício. Link abriu a redefinição, a interface voltou ao login, a nova senha autenticou e o mesmo link foi rejeitado ao reutilizar. [Volta ao login](../output/qa-e2374dd/recuperacao-volta-login.png) · [Uso único](../output/qa-e2374dd/token-reutilizado-negado.png) |
| Sessão expirada, falha de rede e preço alterado no checkout | Passou | Mudança de preço invalida a confirmação com 409 e exige nova revisão. Interrupção do envio exibe o caminho para Meus pedidos. Perda do cookie orienta a entrar novamente. [Preço](../output/qa-e2374dd/revisao-preco-alterado.png) · [Rede](../output/qa-e2374dd/checkout-falha-confirmacao.png) · [Sessão](../output/qa-e2374dd/checkout-sessao-expirada.png) |
| Catálogo, busca, filtro e ordenação | Passou | Busca por Whey, ordenação por menor preço e filtro de preço máximo que remove e restaura o produto. [Catálogo](../output/qa-e2374dd/catalogo-busca-filtros.png) |
| Detalhe do produto e favoritos próprios | Passou | Produto aberto, favorito adicionado, verificado na lista e removido. A lista pertence à sessão do cliente. [Favoritos](../output/qa-e2374dd/favoritos.png) |
| Publicar avaliação própria em produto ativo | Passou | Envio pela UI, comentário exibido e removido da loja após moderação. O serviço exige usuário autenticado e produto ativo e confere o autor. [Avaliação](../output/qa-e2374dd/avaliacao-cliente.png) |
| Galeria, destaque, ofertas, novidades, kits, marcas, objetivos, contato e conteúdo institucional | Não testado | Galeria e destaque passaram na rodada anterior. As demais rotas foram inventariadas no App e no menu. |
| Sacola, remoção, cupom, entrega e revisão | Passou | Sacola limpa e repovoada, cupom inválido rejeitado, COLAB10 aplicado, entrega em endereço salvo, revisão e pedido no painel. [Cupom inválido](../output/qa-e2374dd/cupom-invalido.png) · [Revisão](../output/qa-e2374dd/cliente-revisao-ENTREGA.png) |
| Retirada como CLIENTE, cartão simulado e entrega em UF fora da área | Não testado | Retirada foi executada como COLABORADOR. CLIENTE tem evidência de retirada na rodada anterior. |
| Pedidos próprios, confirmação e reenvio | Passou | Confirmação pela UI e encaminhamento para Meus pedidos. Reenvio por API respondeu 409. Outra conta recebeu 403 por número e por ID de usuário. [Pedido](../output/qa-e2374dd/cliente-pedido-ENTREGA.png) |
| Perfil e consentimento de comunicações | Passou | Atualizar nome, salvar a opção de comunicações, recarregar e verificar persistência. A tabela de protocolos passou a caber na página móvel após a correção. [Antes](../output/qa-e2374dd/perfil-antes.png) · [Depois](../output/qa-e2374dd/perfil-depois.png) |
| Endereços próprios | Passou | Criar, editar o número e remover. A conta secundária recebeu 403 ao listar endereços do titular. [Endereço](../output/qa-e2374dd/endereco.png) |
| Privacidade RCP-02 | Passou | Solicitação de acesso, protocolo próprio, tentativa de leitura por outra conta 403, atendimento pelo ADMIN e resposta visível ao titular. Correção e exclusão têm fluxos a retestar. [Cliente](../output/qa-e2374dd/privacidade-cliente.png) |
| Quantidade e remoção na sacola | Passou | Aumentar de 1 para 2, diminuir para 1 e remover pelo controle separado. [Sacola](../output/qa-e2374dd/sacola-quantidade.png) |
| Alteração de senha autenticada, consentimento de cookies e acesso direto a favoritos ou perfil de terceiro | Não testado | Fluxos e negativas individuais pendentes. |
| Acesso a ADMIN e cashback de COLABORADOR | Passou | Painel negado na interface. HTTP 403 em pedidos ADMIN e resumo de cashback de colaborador. [Negativa](../output/qa-e2374dd/cliente-admin-negado.png) |

## COLABORADOR

| Função e limite | Estado | Execução e evidência |
| --- | --- | --- |
| Login e área de colaborador, saldo e destino Pix/desconto | Passou | Dois contextos renovados, móvel e desktop. Seleção de Pix e desconto, consulta do saldo e persistência após recarga. [Móvel](../output/qa-e2374dd/colaborador-permissao-false.png) · [Desktop](../output/qa-e2374dd/colaborador-permissao-true.png) |
| Permissão de relatórios e isolamento administrativo | Passou | ADMIN alternou a permissão. Em sessão nova, `/me` mostrou o valor correspondente. Em ambos os casos, painel ADMIN negado na interface e HTTP 403 em exportações e pedidos administrativos. [Negativa](../output/qa-e2374dd/colaborador-admin-negado.png) |
| Relatório próprio com permissão | Bloqueado | A permissão é persistida e visível ao ADMIN, mas a rota do colaborador só renderiza saldo, destino, compra e pedidos. A API oferece saldo, resumo e destino, sem rota de relatórios do perfil. |
| Cupons vinculados, metas e indicadores próprios | Bloqueado | O cupom vinculado participa do checkout e aparece para ADMIN. O candidato não expõe consulta desses dados nem metas ao colaborador. A área de objetivos do catálogo é navegação comercial. |
| Compra, retirada e pedidos próprios | Passou | Login, item na sacola, retirada grátis, revisão, confirmação e Meus pedidos no viewport móvel. [Revisão](../output/qa-e2374dd/colaborador-revisao-RETIRADA.png) · [Pedido](../output/qa-e2374dd/colaborador-pedido-RETIRADA.png) |
| Entrega, favoritos e avaliação como colaborador | Não testado | A compra e a leitura no perfil foram verificadas em retirada. |
| Gestão de estoque, pedidos, usuários, cupons e privacidade de terceiros | Passou | Área administrativa negada; pedidos e exportação respondem 403. As demais operações mutáveis exclusivas não foram tentadas individualmente. |

## ADMIN

| Função e limite | Estado | Execução e evidência |
| --- | --- | --- |
| Login e navegação | Passou | Painel carregado e menus Pedidos, Usuários, Privacidade e Relatórios exercitados. |
| Visão geral, período e filtros combinados | Passou | Recarregar período Hoje, combinar Entrega e Pix em Pedidos, confrontar os resultados com a consulta filtrada e limpar filtros. Conciliação completa dos totais do painel permanece pendente. [Indicadores](../output/qa-e2374dd/indicadores-admin.png) · [Pedidos](../output/qa-e2374dd/pedidos-filtros.png) |
| Pedidos, detalhes, conciliação simulada | Passou | Pedido confirmado pelo cliente localizado no painel e detalhes abertos. Aprovação, recusa e cancelamento pela UI. Aprovação repetida via API devolveu 400. A recusa de pedido pendente preservou o estoque. [Detalhes](../output/qa-e2374dd/pedido-painel.png) · [Recusa](../output/qa-e2374dd/pagamento-recusado.png) |
| Estoque e histórico | Passou | ADMIN tentou saldo negativo, corrigiu o saldo e verificou o motivo no histórico. A sessão do cliente mostrou o novo saldo. [ADMIN](../output/qa-e2374dd/estoque-admin.png) · [Loja](../output/qa-e2374dd/estoque-loja.png) |
| Cadastro, edição, preços, descontos e arquivamento de produtos | Não testado | O desconto foi alterado via API de apoio ao checkout. A edição pela UI e o arquivamento seguem pendentes. |
| Categorias, atributos, destaques e galeria | Não testado | QA-05/07/09 passaram no candidato inicial na [rodada anterior](evidencias-qa-2026-11-05.md). O ensaio anterior da V4 aplicou a migração em MySQL isolado e preservou capa legada. O pacote desta rodada não contém DDL novo. |
| Modelo, prévia, importação transacional e histórico CSV | Passou | Estoque negativo rejeitado. Linha corrigida passou pela prévia e apareceu no histórico de importações. [Rejeição](../output/qa-e2374dd/csv-rejeitado.png) · [Histórico](../output/qa-e2374dd/csv-historico.png) |
| Importação XLSX e controle de Produtos citado no relato | Não testado | Importação XLSX pendente. O controle citado ainda precisa ser identificado. |
| Usuários, busca, criação, perfil e permissão | Passou | Alternância de permissão refletida em sessão renovada. Criação e busca têm evidência anterior, enquanto mudança de perfil fica pendente nesta rodada. |
| Inativação de colaborador e sessão aberta | Passou | ADMIN inativou e reativou a conta. Sessão anterior perdeu acesso ao cashback e um novo login foi rejeitado enquanto inativa. [Sessão antiga](../output/qa-e2374dd/colaborador-inativacao-depois.png) · [Novo login](../output/qa-e2374dd/colaborador-login-inativo.png) |
| Moderação de avaliações | Passou | ADMIN encontrou a avaliação publicada e ocultou. A loja deixou de exibir o comentário após recarregar. [Moderação](../output/qa-e2374dd/avaliacao-ocultada.png) |
| Relatórios e exportações RF38–RF40 | Passou | Relatório Produtos filtrado por nome. Downloads CSV, XLSX e PDF concluídos. Linha do produto conferida no CSV e assinaturas dos arquivos conferidas em XLSX/PDF. Outros tipos e filtros permanecem pendentes. [Exportação](../output/qa-e2374dd/relatorios-admin.png) |
| Cupons, cadastro, edição e inativação | Passou | Criar cupom fixo vinculado a colaborador, preencher valor mínimo e limite, editar o desconto e inativar. O uso válido e inválido foi confirmado no checkout. Enforcement de validade e limite permanece sem execução no navegador. [Cupom](../output/qa-e2374dd/cupom-admin.png) |
| Cashback, ajuste e baixa administrativa | Não testado | Crédito vinculado a pedido avaliado no fluxo cruzado. Ajuste e baixa manual ainda precisam de execução. |
| Privacidade, processamento de solicitação | Passou | Localizar protocolo, registrar decisão e justificativa e observar a resposta na conta titular. [ADMIN](../output/qa-e2374dd/privacidade-admin.png) |

## Fluxo cruzado e regressões corrigidas

| ID | Reprodução no candidato inicial, Edge local | Correção e reteste no pacote final |
| --- | --- | --- |
| QA-10 | CLIENTE, 390 × 844. Abrir Minha conta após registrar um protocolo. Esperado 390 px de largura global. A tabela chegou a 757 px. [Antes](../output/qa-e2374dd/perfil-antes.png) | A tabela de privacidade passou a respeitar a largura do viewport. Com protocolo registrado, o reteste mediu 390 px. [Depois](../output/qa-e2374dd/perfil-depois.png) |
| QA-11 | CLIENTE e COLABORADOR, 1440 × 900 e 390 × 844. Inserir o COLAB10 em produto de R$ 149,90 e aprovar o pagamento. Esperado o mesmo cashback na revisão e no saldo. A revisão anunciava R$ 6,75 e o saldo creditava R$ 7,50. [Revisão anterior](../output/qa-2026-11-05/candidato-revisao-ENTREGA.png) | Sacola e pedido passaram a reutilizar a fórmula do crédito. No pedido 12, subtotal R$ 149,90, desconto R$ 14,99, frete R$ 15,00, total R$ 149,91 e cashback R$ 7,50. O reenvio obteve 409. Estoque 20 no pedido pendente, 19 na aprovação e 20 no cancelamento. O colaborador recebeu R$ 7,50 e voltou ao saldo anterior no cancelamento. [Revisão](../output/qa-e2374dd/cliente-revisao-ENTREGA.png) · [Crédito](../output/qa-e2374dd/cashback-credito.png) · [Cancelamento](../output/qa-e2374dd/pedido-cancelado.png) · [Estorno](../output/qa-e2374dd/cashback-estorno.png) |
| QA-12 | ADMIN e COLABORADOR, 1440 × 900 e 390 × 844. Desativar colaborador com sessão aberta, recarregar a área e consultar cashback. Esperado bloqueio. A sessão antiga recebia HTTP 200. [Antes](../output/qa-e2374dd/colaborador-apos-inativacao.png) | Sessões de login passaram a conferir conta ativa e papel atual antes das rotas privadas. Após desativação, a interface redirecionou para login e a sessão antiga recebeu 403 ao repetir a consulta. [Depois](../output/qa-e2374dd/colaborador-inativacao-depois.png) |
| QA-14 | CLIENTE, 390 × 844. Abrir a sacola com um item e clicar em Aumentar. Esperado mudar de 1 para 2. O botão Remover estava sobre o botão Aumentar e interceptava o toque. [Antes](../output/qa-e2374dd/diagnostico-quantidade.png) | Remover passou a ocupar espaço separado no cartão. Aumento, redução e remoção funcionaram no Edge móvel simulado. [Depois](../output/qa-e2374dd/sacola-quantidade.png) |
| QA-15 | CLIENTE, 390 × 844, JAR do reteste da sacola. Enviar recuperação, abrir o link capturado e redefinir a senha. Esperado retornar ao login. A URL retornava a /entrar, mas o formulário de redefinição permanecia visível. [Confirmação de redefinição](../output/qa-e2374dd/senha-redefinida.png) | O modo de login é restaurado na transição. No JAR final, login com nova senha funcionou e a reutilização do link foi recusada. [Retorno](../output/qa-e2374dd/recuperacao-volta-login.png) |

## Validação do pacote

- `pnpm.cmd lint`, `pnpm.cmd build` e `mvnw.cmd package` passaram. Suíte Java com 86 testes registrados, 0 falhas, 0 erros e 1 ignorado. A seleção final de checkout, estoque/pagamento, cashback, CSRF e privacidade passou com 14 testes. O teste de reconciliação agora usa valores que distinguem a base bruta da base após desconto.
- O Spring subiu no MySQL isolado com Flyway V4 atualizado e Hibernate em `validate`. Não houve alteração de esquema nesta rodada. O [ensaio de V4 a partir da V3](evidencias-qa-2026-11-05.md) pertence à rodada anterior.
- A rodada registrou as diferenças de versão entre Render e o candidato. O pacote final foi reempacotado após as alterações de CSS e de navegação no React, com os mesmos testes Java já executados. Os hashes dos bundles servidos conferem com os arquivos estáticos. O código corrigido ainda está na árvore de trabalho. O SHA do JAR identifica o pacote efetivamente usado no reteste.

## Dependências e risco

- **Comercial e jurídico.** Endereço e horário de retirada, prazo de BH e região, ativação e preços FTW, bases legais, retenção, políticas e condições do pagamento real aguardam as decisões registradas na matriz canônica.
- **Integrações.** Gateway, entrega real de e-mail e handoff real para WhatsApp não foram exercitados. O pagamento usado nesta rodada é simulado. A entrega por transportadora não se aplica à operação atual de entrega própria.
- **Acesso.** Render foi conferido por leitura. A versão pública diverge do candidato, e a homologação pública autenticada exige autorização.
- **Aparelhos.** Edge em viewport móvel simulado. Compatibilidade com iPhone/Safari, Android e aparelhos físicos permanece pendente.
- **Cobertura.** Faltam cenários por função assinalados acima e repetição de parte da cobertura anterior sobre o pacote corrigido. Relatórios, cupons vinculados, metas e indicadores na área do colaborador precisam de escopo funcional e uma interface que permita homologação.

## Arquivos e evidências

- [Guia de operação](guia-operacao-qa.md), [backlog](technical-backlog.md) e [painel](../PAINEL_ACOMPANHAMENTO.md).
- [Capturas desta rodada](../output/qa-e2374dd/) e [evidência anterior](evidencias-qa-2026-11-05.md).
- Fonte e segurança: `frontend/src/pages/AuthPage.tsx`, `frontend/src/styles/global.css`, `AccountSessionFilter.java`, `SecurityConfig.java`, `AuthController.java`, `CashbackService.java`, `CarrinhoService.java` e `PedidoService.java`.
- Regressão: `CheckoutReconciliacaoIntegrationTest.java`. Pacote: `src/main/resources/static/index.html`, `index-BVBiYByX.js` e `index-C9mIINJx.css`.
- Documentação: este parecer, `docs/technical-backlog.md` e `PAINEL_ACOMPANHAMENTO.md`.
