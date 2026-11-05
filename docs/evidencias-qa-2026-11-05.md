# Homologação VitaFortis — 05/11/2026

## Ambientes

- **Público, somente leitura.** O Render informou o SHA `0ac066878cd3d2b896b499cc0392de6c30d7432f` e pagamento `SIMULADO`. O verificador confirmou HTTP 200, hashes de `index-BF-mC3lG.js` e `index-52AEm6TZ.css` e 219 produtos ativos. No Edge a 390 × 844, foi aberta a aba Cadastrar sem submeter dados. [Tela pública](../output/qa-2026-11-05/publico-cadastro.png)
- **Local.** Spring e MySQL 8.0.44 em loopback, com banco dedicado, perfis e dados fictícios. Cenários mutáveis foram executados apenas nesse ambiente. O ensaio da V4 partiu de cópia do esquema anterior, com baseline local 3. O Flyway aplicou somente a V4, o Hibernate validou o esquema e a capa preexistente manteve a URL.
- **Navegador.** Edge automatizado via Playwright. Cada perfil usou um contexto de navegador separado. As dimensões são viewports simulados.

## Reprodução antes da correção

| Item | Perfil e tela | Passos | Esperado | Obtido |
|---|---|---|---|---|
| QA-01 | Visitante, Entrar → Cadastrar, 390 × 844 | Preencher cadastro válido e interromper GET do token CSRF antes de enviar | Aviso com instrução para recuperar | Aviso técnico `Failed to fetch`. [Captura](../output/qa-2026-11-05/antes-cadastro-rede.png) |
| QA-01 | Visitante, Entrar → Cadastrar, 390 × 844, MySQL isolado | Enviar CPF válido exibido com máscara | Cadastro criado | Falha HTTP 500. MySQL rejeitou o CPF formatado de 14 caracteres em coluna de 11. |
| QA-01 | ADMIN, Usuários → Novo colaborador, 390 × 844 | Digitar e apagar o nome | Modal continua aberto | Modal fecha e os demais campos ficam inacessíveis. [Captura](../output/qa-2026-11-05/antes-modal-usuarios.png) |
| QA-02 | CLIENTE, sacola, 390 × 844 | Adicionar produto como visitante, entrar, revisar retirada e confirmar | Pedido pendente de aprovação simulada | Pedido local criado. [Revisão](../output/qa-2026-11-05/antes-revisao-RETIRADA.png) · [Pedido](../output/qa-2026-11-05/antes-pedido-RETIRADA.png) |
| QA-03/05/06 | ADMIN, Produtos, 390 × 844 | Abrir Produtos e conferir barra, menu e primeira linha | Menu legível e comandos identificáveis | A faixa corta “Categorias e atributos” e o título aparece como “Products”. Os botões da planilha estão visíveis. O feedback não identifica o controle adicional por nome, posição ou captura. [Captura](../output/qa-2026-11-05/antes-produtos.png) |


## Retestes no pacote candidato

| QA | Perfil e viewport | Passos e resultado | Evidência |
|---|---|---|---|
| 01 | Visitante e ADMIN, 390 × 844 | Cadastro público com CPF inválido, interrupção de rede, reenvio após correção, confirmação e e-mail duplicado. No painel, apagar o nome manteve o modal aberto. CPF inválido, duplicidade, HTTP 503 e sessão expirada apareceram no próprio formulário. Após salvar dados válidos, o colaborador apareceu em Usuários. | [Cadastro](../output/qa-2026-11-05/cadastro-confirmado.png) · [Rede](../output/qa-2026-11-05/cadastro-recuperacao-rede.png) · [Colaborador](../output/qa-2026-11-05/colaborador-confirmado.png) · [HTTP 503](../output/qa-2026-11-05/colaborador-indisponibilidade.png) |
| 02 | CLIENTE e ADMIN, 390 × 844 e 1440 × 900 | Produto → sacola → login → revisão → confirmação. Entrega com cupom de R$ 14,99, frete de R$ 15,00 e total de R$ 149,91. Retirada sem frete, total de R$ 149,90. Pedidos 2 e 3 ficaram pendentes no banco de teste. Repetir a confirmação do pedido 2 devolveu HTTP 409. O ADMIN abriu o pedido 2 e aprovou o pagamento simulado. Saldo 20 → 19 na primeira aprovação e 19 após a rejeição da repetição. | [Entrega](../output/qa-2026-11-05/candidato-revisao-ENTREGA.png) · [Retirada](../output/qa-2026-11-05/candidato-revisao-RETIRADA.png) · [Pedido no painel](../output/qa-2026-11-05/pedido-painel.png) · [Aprovação](../output/qa-2026-11-05/pedido-aprovado.png) |
| 02/03 | CLIENTE, 360 × 800 e 390 × 844 | Alteração de preço depois da revisão devolveu HTTP 409 e nova revisão com valores atualizados. Após zerar o estoque de outro produto, a confirmação devolveu HTTP 400 com aviso de indisponibilidade. Falha durante confirmação ofereceu link para Meus pedidos. Remover o cookie exibiu a orientação de sessão expirada. | [Preço](../output/qa-2026-11-05/revisao-preco-alterado.png) · [Estoque](../output/qa-2026-11-05/checkout-estoque-alterado.png) · [Rede](../output/qa-2026-11-05/checkout-falha-confirmacao.png) · [Sessão](../output/qa-2026-11-05/checkout-sessao-expirada.png) |
| 03 | Visitante, CLIENTE, ADMIN e COLABORADOR | Conferência das ações de cadastro, sacola, galeria, menu e tabelas nos cenários acima. As verificações de largura executadas não encontraram rolagem horizontal global. COLABORADOR entrou com o perfil de teste e recebeu acesso restrito ao visitar o painel ADMIN. | [Menu móvel](../output/qa-2026-11-05/menu-categorias-mobile.png) |
| 04 | ADMIN, 390 × 844. CLIENTE via API na sessão de navegador | Abrir Produtos, localizar Whey, tentar saldo negativo, corrigir para 22 e salvar com motivo. O saldo anterior era 20. A tela mostrou 22, motivo e responsável. CLIENTE recebeu HTTP 403 ao tentar ajustar. | [Saldo e histórico](../output/qa-2026-11-05/estoque-saldo-historico.png) |
| 05 | ADMIN, 390 × 844 | Criar uma categoria e um atributo, inativar e reativar ambos na listagem. Conferir texto integral no menu. | [Categoria](../output/qa-2026-11-05/categoria-reativada.png) · [Atributo](../output/qa-2026-11-05/atributo-reativado.png) |
| 06 | ADMIN, 1440 × 900 | Conferir o modelo, enviar uma linha com estoque negativo, confirmar que a prévia bloqueia a importação, corrigir e reenviar, confirmar e localizar o registro no histórico. O controle adicional mencionado no relato segue sem identificação. | [Rejeição](../output/qa-2026-11-05/csv-rejeitado.png) · [Histórico](../output/qa-2026-11-05/csv-historico.png) |
| 07 | ADMIN e visitante, 390 × 844 | Marcar Whey em Produtos, salvar e conferir o cartão na home. Desmarcar, salvar e conferir estado vazio. | [Seleção](../output/qa-2026-11-05/destaques-selecao.png) · [Vazio](../output/qa-2026-11-05/destaques-vazio.png) |
| 08 | ADMIN, 390 × 844 | Filtrar por parte do e-mail, verificar consulta sem correspondência e limpar o filtro. | [Lista de usuários](../output/qa-2026-11-05/colaborador-confirmado.png) |
| 09 | ADMIN e visitante, 390 × 844 | Abrir produto com capa legada, fazer upload, promover a foto nova para a primeira posição, salvar, alternar para a foto original na loja, remover a nova foto e salvar. Flyway V4 e validação Hibernate passaram no MySQL isolado. | [Editor](../output/qa-2026-11-05/galeria-editor.png) · [Loja](../output/qa-2026-11-05/galeria-publica.png) |

Nos últimos retestes o limite de autenticação do ambiente local subiu de 10 para 100 tentativas por janela, para acomodar os contextos de navegador na mesma origem. O cenário de limite continua coberto pela suíte Java.

## Build e entrega

- `pnpm.cmd lint`, `pnpm.cmd build`, `mvnw.cmd package` e `git diff --check` passaram. A suíte Java registrou 86 testes, 0 falhas, 0 erros e 1 ignorado. Os testes direcionados anteriores registraram 23 testes sem falhas. O teste novo de CPF formatado passou na suíte do pacote.
- Bundles do candidato: `index-CUz5ko25.js` e `index-6Q_CP6RN.css`. O JAR local testado foi `target/demo-0.0.1-SNAPSHOT.jar`, SHA-256 `73dbb76164a13a6ae91e899de4c0a988da13be55307e7405a5f4f6824d878b20`.
- Pendências: identificar o controle sem descrição de QA-06, testar em aparelhos físicos e homologar o candidato no Render após autorização. Gateway real, parâmetros comerciais pendentes da retirada e política de exclusão definitiva dependem de decisão do responsável.
- [Guia para operação e reteste](guia-operacao-qa.md)


## Arquivos alterados

- React: `AuthContext.tsx`, `AuthPage.tsx`, `CartPage.tsx`, `AdminPage.tsx`, `HomePage.tsx`, `ProductPage.tsx`, `CatalogAdmin.tsx`, `StockEditor.tsx`, `ProductGallery.tsx`, `services/api.ts`, `types/api.ts` e `styles/global.css`.
- Java: `UsuarioService.java`, `UsuarioRepository.java`, `ProdutoService.java`, `Produto.java`, `ProdutoMapper.java`, `ProdutoRequestDto.java` e `ProdutoResponseDto.java`.
- Banco e testes: `V4__cria_galeria_produto.sql`, `HomologacaoFluxosIntegrationTest.java`, `FlywaySafetyTest.java` e `PrecoProdutosMigrationTest.java`.
- Entrega: `static/index.html`, os dois bundles referenciados, `guia-operacao-qa.md`, este relatório, capturas em `output/qa-2026-11-05` e `PAINEL_ACOMPANHAMENTO.md`. `technical-backlog.md` foi atualizado na cópia de trabalho, junto às alterações que já existiam.
