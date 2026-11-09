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

Para repetir o teste do catálogo, instale as dependências com `pnpm.cmd install --frozen-lockfile` em `frontend`, inicie `pnpm.cmd dev --host 127.0.0.1` e rode `pnpm.cmd test:catalog` em outro terminal com Google Chrome instalado. `QA_URL` permite apontar para outro servidor local ou para um build local em preview. O teste intercepta as requisições, então não depende de banco ou catálogo real.

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

## Próxima rodada no Render — 06/11/2026

### Liberação do candidato

1. Confirmar em leitura, com o responsável pelo banco, que o host/schema configurado para o serviço do tester é dedicado à QA e separado do banco real. Registrar a comparação de forma redigida, sem string de conexão, credenciais ou dados de clientes.
2. No Render, o serviço está vinculado à `main` e o Auto-Deploy está em `On Commit`. O deploy pelo seletor de commit muda para `Off` e exige autorização específica. A API oficial aceita `commitId` sem alterar o Auto-Deploy. Usar essa rota se houver acesso autorizado e manter o vínculo e as demais configurações.
3. Publicar o SHA completo `d1d4a938183f6b93f26282fee9d1d37dfd500f5c` no mesmo serviço. Após o deploy, exigir HTTP 200 em `/api/v1/loja/versao`, igualdade com o SHA completo e `pagamento: SIMULADO`. Buscar o HTML da loja e verificar HTTP 200, tipo de conteúdo e hashes dos JS/CSS que ele referencia, comparando com o build do candidato. Em caso de divergência, registrar esperado e observado e interromper antes de liberar o reteste.

### Preparação e restauração

- Usar sessões isoladas de ADMIN, CLIENTE e dois COLABORADORES fictícios, com registros distintos. Reutilizar contas de QA quando possível. Registrar o SHA, horário, perfil e o estado inicial dos SKUs, saldos, estoque, permissões, atividade, cupons, pedidos e sacolas envolvidos. Conferir que os SKUs novos não existem. Se houver colisão, reservar novos códigos antes de importar para evitar atualizar um registro existente.
- Usar um banco de QA com ponto de restauração e uma janela exclusiva combinados com o responsável. Após o teste, cancelar e estornar os pedidos simulados, conferir saldo e estoque, restaurar permissões, atividade, cupons e sacolas. Restaurar a ausência dos registros criados via ponto de restauração do banco de QA se a interface não permitir removê-los. Exportar antes apenas a evidência redigida, e confirmar o estado final contra o inicial. Se não houver rota segura de restauração, suspender o cenário que grava dados.

### Produto e estoque

1. Com ADMIN, abrir **Produtos → Novo produto**. Cadastrar `QA-D1D4A93-7F2-MANUAL`, com nome, descrição e marca fictícios, unidade 300g, categoria PROTEINAS ativa, preço de R$ 12,50, estoque 2 e situação inativa. Usar os campos obrigatórios exibidos pelo formulário.
2. Recarregar, localizar pelo SKU incluindo inativos e conferir todos os campos salvos e a ausência na loja pública. Em **Ajustar estoque**, informar saldo total 5 e motivo fictício. Recarregar e confirmar saldo 5, produto inativo e histórico com saldo anterior 2, saldo novo 5, motivo, data e responsável.
3. Se o formulário estiver inacessível, registrar perfil, tela, ação, comportamento observado e mensagem ou HTTP disponível. O relato anterior cobre somente a adição de estoque, com SHA desconhecido. Cadastro completo permanece **Não testado**.

### Recuperação de senha e criação de conta

1. Recuperação fica **Bloqueado** até haver uma conta fictícia vinculada a uma caixa de QA controlada e autorizada. Preferir conta existente. O relato de que o tester não conseguiu criar uma conta é uma ocorrência separada, sem mensagem, código HTTP ou SHA identificado.
2. Se for necessário criar conta, reproduzir em **Entrar → Cadastrar** no ambiente isolado com os dados de QA autorizados. Anotar perfil/sessão, tela, passo, mensagem redigida e status HTTP do cadastro, ou ausência de requisição se a validação impedir o envio. Propor correção somente após a reprodução.
3. Em **Esqueci minha senha**, solicitar a recuperação, confirmar o recebimento na caixa, redefinir, entrar com a nova senha e verificar a rejeição ao reutilizar o mesmo link. Registrar resultado e status de cada passo, omitindo credenciais, endereço da caixa, tokens e links. Preservar a evidência anterior do SMTP em loopback.

### Importação de produtos

Os arquivos CSV e XLSX usam as dez colunas do modelo de **Produtos → Baixar modelo CSV**, categoria `PROTEINAS` e situação inativa. Confirmar que a categoria está ativa antes da prévia.

1. Enviar [vitafortis-qa-previa.xlsx](../outputs/01a1b304-c316-7be1-bc85-49bf39364a5c/vitafortis-qa-previa.xlsx). A prévia deve indicar uma inserção para `QA-D1D4A93-7F2-XLSX` e rejeição da linha 3, `QA-D1D4A93-7F2-NEG`, com estoque −1. A confirmação do arquivo misto deve ficar impedida e ambos os SKUs devem continuar ausentes.
2. Enviar [vitafortis-qa-valido.csv](../outputs/01a1b304-c316-7be1-bc85-49bf39364a5c/vitafortis-qa-valido.csv). Confirmar na prévia uma inserção, zero rejeições e estoque 3 para `QA-D1D4A93-7F2-CSV`. Conferir que a prévia ainda não gravou o SKU. Confirmar a importação transacional e conferir o histórico com arquivo, data, responsável, uma inserção e resultado concluído. Recarregar e conferir SKU, descrição, marca, unidade, preço, categoria, estoque 3, imagem vazia e situação inativa.
3. Para confirmar também o XLSX, salvar uma cópia removendo a linha 3 inteira. Repetir prévia e confirmação para o SKU `QA-D1D4A93-7F2-XLSX`, com estoque 4, e conferir histórico e persistência. O SKU inválido deve continuar ausente. Restaurar o estado inicial pelo plano combinado.
4. Para o tester, manter **Não testado — arquivo indisponível** até confirmar recebimento e execução. A validação local dos arquivos fica registrada separadamente.

### Catálogo, colaborador e Pix simulado

- **Catálogo:** em desktop e celular, alternar rapidamente busca, filtros de preço, limpeza e paginação, incluindo respostas fora de ordem e falhas. Confirmar que a lista, o total, a página e o estado de carregamento correspondem à última consulta.
- **Colaborador:** conceder permissão a uma conta fictícia e conferir extrato e cupons próprios. Comparar com a segunda conta, verificar que nenhum registro cruza titulares, testar área ADMIN restrita, revogar e repetir com sessão nova e antiga. Inativar a conta fictícia e verificar sessão antiga invalidada e novo login rejeitado.
- **Pix simulado:** registrar estoque e saldo iniciais. Criar revisão com cupom fictício vinculado ao colaborador, conferir total e cashback sobre o subtotal. Confirmar, reenviar a confirmação e checar pedido único. Aprovar como ADMIN e conferir baixa única e crédito. Cancelar e conferir estorno e recomposição. Registrar os resultados sem dados pessoais.

Metas, indicadores, endereço e horário de retirada e prazo para BH/região aguardam decisão do responsável.
