# VitaFortis — preparação do reteste de 05/11/2026

Base: branch `qa/vitafortis-2026-11-05`, SHA `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`. O relatório de referência do Render confirmou esse SHA e `pagamento: SIMULADO`. As alterações deste reteste estão na árvore local para revisão; nenhum commit, push, deploy ou alteração de configuração do Render foi feito.

| Área | Validação local | Render |
| --- | --- | --- |
| Busca do catálogo (R94-01) | Reprodução no Edge em 1440 × 900 e 390 × 844 com toque: 1 resultado filtrado, seguido de 219 após liberar a resposta inicial, com o termo ainda ativo. A regressão intercepta as respostas e controla sua ordem, inclusive falhas durante e após a requisição vigente, mudanças de preço, limpeza e paginação. | O relatório anterior reproduziu a falha; a correção ainda não foi publicada. |
| Recuperação (RF07/RF19) | Edge móvel simulado, banco isolado e SMTP em loopback: solicitação 200, recebimento, abertura do link, redefinição 200, retorno ao login, login com nova senha 200 e reutilização 400. Evidência deliberadamente sem mensagens, links, tokens ou credenciais. | **Bloqueado:** não há caixa de QA autorizada para receber a mensagem da configuração ativa. Isso não comprova falha de envio. |
| Colaborador (QA-13, RF22/RF25, RN07, RF23/RN05) | Edge com Spring e banco isolado: relatório e cupom exibidos com permissão, seção ausente em nova sessão sem permissão e painel ADMIN restrito. A API retorna 403 após revogação, invalida a sessão anterior da conta inativada (401) e rejeita novo login (403). A integração Java usa duas contas fictícias com registros distintos e verifica que forjar um ID não muda o titular. | Pendente de publicação e reteste com duas contas de QA. |
| Checkout, cashback e estoque (RF17, RF23/RN05, RN03) | Teste do checkout local cobre revisão expirada, concorrência e reenvio, além de aprovação simulada, baixa única, crédito sobre subtotal, cancelamento, estorno e recomposição de estoque. | Cenários passaram no SHA anterior, conforme relatório de referência; repetir no novo SHA. |

A mudança da URL disparava outra busca sem invalidar os callbacks anteriores. Eles ainda podiam preencher a lista, corrigir a página, exibir erro ou terminar o carregamento. Agora cada carregamento invalida seus callbacks quando a consulta muda, quando a página é desmontada ou quando outra tentativa começa.

O relatório próprio expõe data, tipo e valor do lançamento e código, status cadastral e percentual de cashback do cupom. O saldo e a opção Pix/desconto continuam disponíveis sem a permissão de relatórios. O relatório não devolve pedidos, dados de clientes, identidade do operador ou justificativas administrativas. O status cadastral do cupom não indica elegibilidade em uma compra.

Para repetir o teste do catálogo: em `frontend`, instale com `pnpm.cmd install --frozen-lockfile`, inicie `pnpm.cmd dev --host 127.0.0.1` e rode `pnpm.cmd test:catalog` em outro terminal com Microsoft Edge instalado. `QA_URL` permite apontar para outro servidor local ou para um build local em preview. O teste intercepta as requisições, então não depende de banco ou catálogo real.

Arquivos para revisão:

- Busca e teste Edge: `frontend/src/pages/CatalogPage.tsx`, `frontend/tests/catalog-race.mjs`, `frontend/package.json` e `frontend/pnpm-lock.yaml`.
- Relatório próprio: `CollaboratorPage.tsx`, `frontend/src/services/api.ts`, `frontend/src/types/api.ts`, `CashbackController.java`, `CashbackService.java` e `ColaboradorRelatorioDto.java`.
- Regressões Java: `ColaboradorCompraIntegrationTest.java` e `CheckoutReconciliacaoIntegrationTest.java`.
- Estado e roteiro: `PAINEL_ACOMPANHAMENTO.md` e este relatório.

O build local com lockfile fixo gerou `index-BEUD9Oml.js` e `index-C9mIINJx.css`, ambos referenciados pelo HTML do build. Os assets históricos rastreados foram preservados. O Dockerfile do Render gera o frontend e o copia para os recursos do Spring durante o build.

Checks: `pnpm.cmd lint`, `pnpm.cmd build`, `pnpm.cmd test:catalog` e `git diff --check` passaram. Maven: 14 testes direcionados sem falhas ou ignorados; suíte `mvnw.cmd test` com 87 testes, zero falhas ou erros e um ignorado.

Pendências separadas:

- **Integração:** confirmar qual caixa de QA controlada está autorizada para o SMTP do Render. O `render.yaml` versionado tem `EMAIL_ENABLED=false`; a configuração documentada no README prevê relay Brevo, remetente, autenticação e STARTTLS. O backend usa `JavaMailSender` quando habilitado e monta o link a partir de `FRONTEND_URL`. O estado do ambiente ativo não foi inferido do Blueprint. Gateway real permanece fora deste reteste.
- **Permissões:** decidir se a consulta a cupons próprios deve ganhar uma permissão independente do relatório. Neste recorte, ambos integram a mesma consulta protegida. Repetir concessão, revogação, isolamento e inativação no novo candidato.
- **Regras comerciais:** quais metas e indicadores devem aparecer, quem os define, qual a fonte, período e fórmula de cada um e como cancelamentos entram nos cálculos? Qual o endereço e horário de retirada e qual o prazo para BH e região? Esses dados continuam sem preenchimento.

Após uma publicação autorizada, consultar `/api/v1/loja/versao` e exigir HTTP 200, SHA completo do candidato publicado e `pagamento: SIMULADO` antes de executar o roteiro no Render. Conferir os hashes dos assets, retestar as duas dimensões no Edge, usar sessões renovadas e manter a recuperação como **Bloqueado** até a caixa de QA estar disponível. A simulação móvel não substitui o reteste em aparelho físico.
