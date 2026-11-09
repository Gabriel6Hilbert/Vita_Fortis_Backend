# Painel de acompanhamento - Vita Fortis

Este é o ponto de entrada para acompanhar o projeto no iPhone, no PC e em novos chats com IA.

## Acesso rápido

- [Mapa dos arquivos](MAPA_ARQUIVOS.md)
- [Modelo de pedido para IA](MODELO_PEDIDO_IA.md)
- [Status completo dos requisitos](STATUS_CONSOLIDADO_V2.md)
- [Como executar o projeto](README.md)

## Legenda

- ⬜ A fazer
- 🟡 Em andamento
- 🔵 Implementado, aguardando teste
- 🟣 Testado localmente
- 🟢 Publicado e verificado
- 🔴 Bloqueado por decisão, acesso ou credencial

## Etapa atual

### Organização do trabalho com IA

Estado: 🟢 Publicado e verificado

- [x] Criar regras automáticas de escopo no `AGENTS.md`.
- [x] Criar mapa inicial de arquivos.
- [x] Criar modelo de solicitação pequena e rastreável.
- [x] Criar painel legível no celular e no computador.
- [x] Revisar e autorizar a organização com o proprietário.
- [x] Publicar no GitHub após aprovação explícita.
- [ ] Confirmar a abertura dos links no iPhone e no PC.

## Próximas etapas

### Segurança de tráfego

Estado: 🟡 Em andamento

- [ ] Confirmar domínio, DNS e proxy utilizados hoje.
- [ ] Definir países permitidos e exceções necessárias.
- [x] Implementar proteção local contra excesso de tentativas em login, cadastro e recuperação de senha.
- [x] Reforçar validação, expiração, uso único e concorrência dos tokens de recuperação.
- [x] Implementar proteção CSRF vinculada à sessão para operações mutáveis no backend e no frontend.
- [x] Validar rejeição sem CSRF, aceitação com token válido, login, logout e recuperação de senha.
- [x] Validar localmente a proteção com testes Java, lint e build do frontend.
- [x] Publicar a proteção anterior de autenticação no GitHub.
- [x] Enviar a proteção CSRF ao GitHub no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`.
- [x] Homologar a proteção no ambiente público no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9` (26/10/2026).
- [ ] Adicionar proteção complementar na borda para login, recuperação de senha e APIs sensíveis.
- [ ] Evitar acesso que contorne a proteção pelo endereço do Render.
- [ ] Testar bloqueio, rate limit e tráfego brasileiro legítimo.

### Organização do repositório

Estado: 🟡 Em andamento

- [ ] Classificar alterações locais existentes.
- [ ] Identificar bundles ativos e obsoletos.
- [ ] Remover somente arquivos gerados comprovadamente sem uso.
- [x] Sincronizar o bundle React com proteção CSRF no pacote servido pelo Spring (TEC-03, validado localmente em 09/10/2026).
- [ ] Consolidar os documentos de status sem apagar histórico necessário.

### Encerramento técnico

Estado: 🟡 Em andamento

- [x] Validar Flyway em banco isolado e registrar baseline 2 no MySQL real.
- [x] Executar backup real e ensaiar a restauração com 23 tabelas sem divergências.
- [x] Executar 78 testes Java, lint e build do frontend.
- [ ] Homologar ADMIN, CLIENTE, COLABORADOR e RCP-02.
- [ ] Registrar compatibilidade em iPhone, desktop e navegadores.
- [x] Publicar e verificar o mesmo commit homologado.

### Dependências do proprietário

Estado: 🔴 Aguardando decisões ou insumos

- RCP-01 e RCP-03 possuem minutas técnicas locais; ainda faltam retenção, decisão sobre tecnologias de publicidade, prazo de BH/região, revisão e aprovação jurídica.
- Gateway e regras de pagamento real.
- Provedor SMTP, remetente e modelos de e-mail.
- Decisão sobre transportadora externa.
- Preço, estoque e ativação dos produtos FTW importados.

## Registro da tarefa atual

Objetivo: testar, publicar e homologar o pacote operacional consolidado.

Arquivos desta tarefa:

- `src/main/java/VitaFortis/demo/config/SecurityConfig.java`
- `src/main/java/VitaFortis/demo/config/CsrfController.java`
- `frontend/src/services/api.ts`
- testes de integração e segurança em `src/test/java`
- `PAINEL_ACOMPANHAMENTO.md`

Estado comprovado até 29/10/2026:

- release funcional `c875f7af465eff16ec287fa30a94042bb7d203b5` publicada no Render e verificada com HTTP 200, SHA e hashes dos bundles correspondentes;
- rotas de catálogo e políticas e o endpoint CSRF responderam HTTP 200;
- 181 produtos ativos foram encontrados e o pagamento permanece identificado como `SIMULADO`;
- validação local concluída com 78 testes Java sem falhas ou erros, lint e build React e pacote Spring;

- CSRF enviado ao GitHub no SHA completo `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`;
- homologação pública do CSRF concluída em 26/10/2026: HTTP 200, SHA completo, bundles do commit com hashes correspondentes, token e cookie de sessão, rejeição sem token, aceitação com token válido, login CLIENTE, logout autenticado e recuperação de senha comprovados;
- regras comerciais do commit funcional `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4` publicadas pelo commit `c72100a1d8ec4015c53d4bf65b4ed079781c6c54`: UFs MG/SP/RJ/ES, retirada sem frete, preparação em até 24 horas, entrega em até 5 dias úteis fora de BH e região, frete grátis somente acima de R$ 200,00, Pix/cartão e conciliação por ADMIN;
- prazo de BH e região, tarifa abaixo do limite, endereço/horário de retirada, transportadora e integrações reais continuam pendentes;
- pagamento permanece simulado; o checkout comercial publicado ainda precisa de smoke test funcional completo.

Última atualização deste painel: 29/10/2026. A release `c875f7af465eff16ec287fa30a94042bb7d203b5` foi publicada e verificada no Render. O pagamento continua simulado; a homologação autenticada completa dos três perfis e os testes formais em aparelhos e navegadores permanecem pendentes.

## Homologação local QA-01 a QA-09 — 05/11/2026

Estado: 🟣 Candidato testado localmente no Edge e em MySQL isolado. O pacote Java passou com 86 testes, zero falhas e um ignorado. O fluxo CSV passou e a identificação do controle adicional citado em QA-06 segue pendente. O ambiente público servia `0ac066878cd3d2b896b499cc0392de6c30d7432f` na conferência de leitura. A publicação do candidato e o smoke test público dependem de autorização.

- [Matriz de cenários e capturas](docs/evidencias-qa-2026-11-05.md)
- [Guia de operação](docs/guia-operacao-qa.md)

## Homologação funcional de 05/11/2026

Estado atual: 🟡 Homologação parcial. A liberação integral segue pendente.

- Candidato local inicial `e2374dde8c2fc5b333ba5c96d4236dd32a91c8a6`. Render conferido somente em leitura, com SHA `0ac066878cd3d2b896b499cc0392de6c30d7432f` e pagamento simulado.
- A rodada registrou evidências dos três perfis em banco isolado. Foram tratadas a largura da tabela de privacidade, os controles da sacola móvel, a divergência da previsão de cashback, a sessão de conta inativa e o retorno ao login após recuperação.
- A permissão de relatórios do colaborador foi conferida em sessões renovadas. A área do perfil não apresenta relatório próprio. A homologação física e pública do candidato continua pendente.
- [Matriz e parecer](docs/homologacao-funcional-2026-11-05.md) · [Guia de operação](docs/guia-operacao-qa.md) · [Backlog](docs/technical-backlog.md).

### Preparação do reteste sobre `94d09df`

- A homologação de referência confirmou `94d09df` e pagamento simulado no Render. A busca falhou com respostas fora de ordem; a correção passou no Edge local em desktop e celular simulado.
- O relatório local do colaborador passou a mostrar somente o extrato e os cupons da conta com permissão. Metas e indicadores continuam aguardando definição comercial.
- Recuperação validada localmente com captura SMTP. O Render continua bloqueado para esse cenário até haver uma caixa de QA autorizada.
- [Evidências, pendências e roteiro de reteste](docs/reteste-candidato-2026-11-05.md).

### Candidato no GitHub e homologação no Render — 05/11/2026

- HEAD local e branch remota `qa/vitafortis-2026-11-05` em `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`. A `main` remota estava em `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`.
- A última identificação registrada associa `https://vita-fortis-backend.onrender.com` ao serviço `Vita_Fortis_Backend`, ambiente `Production`, branch `main`. A versão anterior registrada retornou HTTP 200, SHA `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e `pagamento: SIMULADO`. Nesta rodada, as consultas de leitura ao endpoint expiraram sem resposta HTTP. A consulta ao painel foi interrompida pela ferramenta de navegação ao tentar identificar a URL ativa no Windows.
- **Deploy bloqueado.** Serviço, URL e branch de homologação com banco isolado de produção não foram comprovados. O responsável precisa disponibilizar ou identificar o serviço isolado, sua URL, o vínculo com a branch de QA e a confirmação do banco dedicado, além do acesso de leitura para verificá-los e da rota de deploy já configurada.
- Checks locais do candidato registrados no roteiro: `git diff --check`, `pnpm.cmd lint`, `pnpm.cmd build`, `pnpm.cmd test:catalog` com Vite e Edge (1440 × 900 e móvel simulado 390 × 844 com toque), e `mvnw.cmd test` (87 testes, zero falhas ou erros, um ignorado). Código preservado nesta rodada.

| Cenário no Render | Estado | Evidência |
| --- | --- | --- |
| Versão, pagamento simulado e assets do candidato | Bloqueado | Aguardando rota isolada e publicação. O endpoint conhecido expirou sem resposta HTTP. |
| Catálogo e respostas obsoletas | Não testado | Evidência local no roteiro do candidato. |
| Colaborador, isolamento, revogação, ADMIN e inativação | Não testado | Evidência local no roteiro do candidato. |
| Pix simulado, reenvio, cashback e estoque | Não testado | Evidência local no roteiro do candidato. |
| Recuperação de senha | Bloqueado | Caixa controlada e autorizada pendente. Validação local com SMTP em loopback, incluindo rejeição da reutilização. |

Nenhum dado de QA foi alterado ou criado nesta rodada. Os registros fictícios da rodada anterior seguem documentados no relatório de referência.

- **Integração:** disponibilizar caixa controlada já autorizada para recuperação no Render. Gateway real segue fora do reteste simulado.
- **Permissões:** disponibilizar acesso ao serviço isolado, retestar isolamento, revogação e inativação e definir se cupons próprios terão permissão independente do relatório.
- **Regras comerciais:** metas, indicadores, endereço e horário de retirada e prazo para BH/região aguardam definição do responsável.

**Parecer:** manter a liberação pendente até comprovar o SHA, o modo de pagamento e os assets no ambiente isolado e concluir os cenários no Render.

#### Retomada da homologação no Render — 05/11/2026

- O HEAD local e a branch remota `qa/vitafortis-2026-11-05` foram reconfirmados em `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`. A `main` remota foi reconfirmada em `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`, ancestral direto do candidato.
- Após o timeout da rodada anterior, o endpoint público conhecido respondeu HTTP 200 com `commit: 94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e `pagamento: SIMULADO`. A identificação registrada associa esse endereço a `Vita_Fortis_Backend`, ambiente `Production`, branch `main`. A leitura confirma o SHA público anterior.
- **Deploy bloqueado antes da publicação.** A ferramenta de navegação interrompeu a consulta por não conseguir identificar com segurança a URL ativa no Windows. Sem acesso autenticado de leitura ao painel ou à API, o serviço de QA, sua URL, seu vínculo com a branch e o banco dedicado continuam sem verificação. O responsável precisa fornecer o nome ou ID do serviço já configurado, a URL, o vínculo com `qa/vitafortis-2026-11-05` e evidência do banco separado de produção, ou acesso de leitura para verificar esses itens. A evidência deve omitir a string de conexão.
- SHA do candidato efetivamente testado no Render nesta retomada: nenhum. A matriz acima continua válida para o candidato. O HTTP 200 público corresponde ao SHA anterior. A versão, o modo de pagamento e os assets do candidato continuam com a validação bloqueada.
- Checks locais do candidato reaproveitados conforme o roteiro acima. Nenhum teste funcional remoto foi iniciado. Nenhum registro de QA foi criado ou alterado nesta retomada, e os registros fictícios anteriores permanecem no relatório de referência.

### Retomada da homologação no Render — 06/11/2026

- `git ls-remote` confirmou `qa/vitafortis-2026-11-05` em `d1d4a938183f6b93f26282fee9d1d37dfd500f5c` (também HEAD local) e `main` em `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` (pai direto do candidato).
- A navegação local está desativada nas configurações do Work. A consulta de leitura `GET /v1/services` à API do Render, sem credencial disponível no ambiente, retornou HTTP 401. Nome ou ID, URL, branch de origem e banco do serviço de QA seguem sem verificação.
- Produção identificada no registro anterior como `Vita_Fortis_Backend` / `Production` / `main`. O endpoint público `/api/v1/loja/versao` em `vita-fortis-backend.onrender.com` expirou após 25 s e 45 s sem resposta HTTP. O último resultado registrado permanece HTTP 200, SHA `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e `pagamento: SIMULADO`.
- **Deploy bloqueado antes da publicação.** SHA do candidato efetivamente testado no Render nesta rodada: nenhum. Para retomar, o responsável precisa fornecer acesso de leitura ao serviço de QA ou evidência do nome/ID, URL, vínculo com a branch de QA, banco separado de produção (omitir a string de conexão) e rota de deploy existente. Se o serviço isolado ainda não existir, sua configuração exige tarefa separada.
- Checks locais reaproveitados para `d1d4a93` conforme o [roteiro](docs/reteste-candidato-2026-11-05.md): `git diff --check`, `pnpm.cmd lint`, `pnpm.cmd build`, `pnpm.cmd test:catalog` com Vite e `mvnw.cmd test` (87 testes, zero falhas ou erros, um ignorado). O teste local de catálogo usou Edge desktop 1440 × 900 e celular simulado 390 × 844 com toque. Nesta retomada foi executado `git diff --check` para a atualização documental.

| Cenário do candidato no Render | Estado | Evidência |
| --- | --- | --- |
| HTTP 200, SHA completo, pagamento simulado e assets do HTML | Bloqueado | Aguarda comprovação do ambiente isolado e deploy. |
| Catálogo e respostas obsoletas | Não testado | Evidência local reaproveitada. |
| Colaborador, isolamento, revogação, ADMIN e inativação | Não testado | Evidência local reaproveitada. |
| Pix simulado, reenvio, cashback e estoque | Não testado | Evidência local reaproveitada. |
| Recuperação de senha | Bloqueado | Falta caixa de QA controlada e autorizada. Validação local com SMTP em loopback cobriu troca, novo login e rejeição de reutilização. |

Nenhum registro foi criado ou alterado nesta rodada. A rodada de referência registrou como remanescentes a conta fictícia #12, o protocolo, os pedidos cancelados #10 e #11, a avaliação ocultada, o cupom desativado e dois registros inativos de importação.

- **Integração:** identificar caixa de QA autorizada para recuperação. Gateway real segue fora do reteste simulado.
- **Permissões:** liberar leitura do ambiente isolado, retestar revogação e inativação e definir se cupons próprios terão permissão independente do relatório.
- **Regras comerciais:** metas, indicadores, endereço e horário de retirada e prazo para BH/região aguardam definição do responsável.

**Parecer:** liberação pendente. O candidato precisa ser publicado na rota isolada e passar pela verificação de versão, pagamento e assets antes do reteste funcional no Render.

#### Identificação do ambiente de homologação pelo responsável

- O responsável confirmou que o serviço no ar, `Vita_Fortis_Backend` em `https://vita-fortis-backend.onrender.com`, é o ambiente usado pelo tester. A identificação anterior como produção veio do rótulo `Production` e da branch `main` registrados no Render. Esses dados ficam preservados como evidência da configuração observada.
- A identificação do serviço está resolvida. Para publicar nele o candidato `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`, falta confirmar se a rota de deploy existente aceita a branch `qa/vitafortis-2026-11-05` sem alterar a `main` e confirmar o isolamento do banco antes dos testes com dados fictícios. Deploy e reteste do candidato seguem pendentes.

#### Conferência pelo Chrome no painel do Render

- O painel autenticado confirmou o serviço de homologação `Vita_Fortis_Backend` (`srv-dahmmkbm8hqs73cfrog0`), URL `https://vita-fortis-backend.onrender.com`, rótulo `Production`, branch `main` e último deploy bem-sucedido em `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`.
- A rota `Manual Deploy > Deploy a specific commit` encontrou o SHA completo do candidato `d1d4a938183f6b93f26282fee9d1d37dfd500f5c` e aceita commits de outras branches. O painel avisa que essa ação desativa o Auto-Deploy. O seletor foi fechado sem publicar. Falta comprovar o isolamento do banco e resolver o efeito sobre o Auto-Deploy antes da publicação, conforme as restrições da homologação.

### Conferência do ambiente e retorno do tester — 06/11/2026

- **Render em leitura.** O painel autenticado de `Vita_Fortis_Backend` mostra o deploy `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` como ativo, no serviço indicado pelo tester, ambiente `Production`, branch `main` e Auto-Deploy `On Commit`. A consulta a `/api/v1/loja/versao` expirou após 70 segundos, sem resposta HTTP. O último HTTP 200 registrado apontava o mesmo SHA e pagamento `SIMULADO`. A leitura de hoje confirma o deploy ativo pelo painel.
- **Publicação bloqueada.** Esperado `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`. Observado no painel `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`. A página Environment confirma configuração externa via `DB_URL`, mas não comprova separação dos dados reais. Falta evidência redigida do banco dedicado. O seletor de SHA desativa o Auto-Deploy e requer autorização específica. A [API oficial de deploy](https://api-docs.render.com/reference/create-deploy) oferece deploy por SHA preservando a configuração, sujeito a acesso autorizado. Nenhum deploy, mudança de configuração ou alteração de dados foi realizado no Render.
- [Roteiro do candidato e arquivos de importação](docs/reteste-candidato-2026-11-05.md#próxima-rodada-no-render--06112026).

| Cenário | Tester | Validação local | Reteste no Render do candidato |
| --- | --- | --- | --- |
| Versão, pagamento e assets | Não testado | Build anterior do candidato documentado no roteiro. | Bloqueado — aguarda comprovação do isolamento e deploy. |
| Adição de estoque | Passou — conforme relato, com SHA não identificado. | Validação anterior preservada. | Não testado |
| Cadastro completo de produto e conferência após recarregar | Não testado | Não testado nesta retomada. | Não testado |
| Criação de conta | Falhou — relato de tentativa sem sucesso, sem SHA, mensagem ou HTTP informado. | Não testado nesta retomada. | Não testado — ocorrência ainda sem reprodução. |
| Recuperação de senha | Bloqueado — falta conta fictícia com caixa de QA autorizada. | Passou — evidência anterior de SMTP em loopback, redefinição, novo login e rejeição da reutilização. | Bloqueado — caixa pendente. |
| Importação CSV/XLSX | Não testado — arquivo indisponível na tentativa. Recebimento e execução pendentes. | Passou — teste dirigido com os arquivos entregues, em H2 em memória. XLSX misto teve uma rejeição na prévia e confirmação recusada sem gravar os SKUs. CSV passou na prévia e confirmação, com histórico concluído e produto inativo com estoque 3. | Não testado |
| Catálogo e respostas obsoletas | Não testado no candidato | Passou — regressão local já registrada. | Não testado |
| Colaborador, isolamento, revogação e inativação | Não testado no candidato | Passou — sessões e integração já registradas. | Não testado |
| Pix simulado, reenvio, cashback e estoque | Não testado no candidato | Passou — integração já registrada. | Não testado |

Metas, indicadores, endereço e horário de retirada e prazo para BH/região permanecem em aberto com o responsável.

### Reteste de ponta a ponta no Render — 07/11/2026

**Liberação bloqueada.** Consultas HTTPS de leitura feitas pelo terminal em Windows desktop no endereço indicado pelo tester, `https://vita-fortis-backend.onrender.com`. O SHA desta rodada ficou indeterminado. O SHA `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e o pagamento simulado pertencem à evidência anterior de 06/11, registrada acima.

| Condição | Estado | Esperado | Observado nesta rodada |
| --- | --- | --- | --- |
| GET `/api/v1/loja/versao` | Falhou | HTTP 200, SHA `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`, pagamento `SIMULADO`. | Timeout de 55 s, zero bytes e nenhuma resposta HTTP (`curl` 28, código de saída HTTP reportado como `000`). SHA e modo de pagamento indeterminados. |
| GET `/` e carregamento dos assets | Bloqueado | HTML disponível e arquivos JS e CSS referenciados carregados. | A consulta ao HTML expirou em 55 s, com zero bytes. As referências JS/CSS não puderam ser extraídas nem verificadas. |
| Banco de QA e restauração | Bloqueado | Evidência redigida de banco dedicado e rota de restauração do estado inicial. | Isolamento e restauração não comprovados. A identificação do serviço pelo tester e a presença de configuração externa, registradas acima, não demonstram esses requisitos. |

O reteste funcional foi interrompido na checagem de liberação. As linhas abaixo indicam os perfis previstos e os critérios a verificar. Nenhuma sessão de perfil foi aberta, nenhum passo funcional foi executado e não houve observação em desktop ou celular para esses cenários. O SHA remoto da rodada é indeterminado para todas as linhas.

| Cenário | Perfil previsto | Estado no Render em 07/11 | Passos e critérios pendentes |
| --- | --- | --- | --- |
| Cadastro de produto | ADMIN | Não testado | Criar produto inativo com SKU exclusivo, R$ 12,50 e saldo 2. Recarregar, localizar pelo SKU, conferir campos e ausência no catálogo público. |
| Ajuste de estoque | ADMIN | Não testado | Ajustar saldo total de 2 para 5 com motivo. Recarregar e conferir saldo e histórico com anterior, novo, motivo, data e responsável. |
| Prévia XLSX misto | ADMIN | Não testado | Conferir categoria PROTEINAS ativa e ausência dos SKUs. Verificar uma inserção prevista, rejeição do estoque −1, confirmação bloqueada e ausência dos dois SKUs. |
| Importação CSV válido | ADMIN | Não testado | Conferir prévia com uma inserção e zero rejeições. Confirmar e recarregar para verificar produto inativo com saldo 3 e histórico concluído. |
| Importação XLSX corrigido | ADMIN | Não testado | Usar cópia sem a linha inválida. Confirmar e verificar produto inativo com saldo 4, histórico concluído e ausência do SKU inválido. |
| Criação de conta | CLIENTE | Não testado | Cadastrar conta fictícia em cenário separado e registrar eventual mensagem e HTTP, ou ausência de requisição. |
| Recuperação de senha | CLIENTE | Bloqueado | Falta caixa de QA controlada e autorizada. Com a caixa, verificar recebimento, redefinição, login com nova senha e rejeição de reutilização do link. |
| Catálogo e ordem das respostas | Público/CLIENTE | Não testado | Alternar busca, preço, limpeza e página em desktop e celular, registrando o tipo de dispositivo. Conferir lista, total e página da última consulta, inclusive com resposta antiga atrasada quando possível. |
| Permissões e isolamento | ADMIN e dois COLABORADORES | Não testado | Conceder relatório a uma conta, verificar extrato e cupons próprios e restrição do ADMIN. Revogar, conferir sessão ativa e nova, inativar, conferir perda de acesso e recusa de novo login e restaurar o estado inicial. |
| Pix simulado, cashback e estoque | CLIENTE e ADMIN | Não testado | Revisar total e cashback sobre subtotal com cupom de colaborador. Reenviar confirmação, aprovar Pix, conferir pedido, baixa e crédito únicos, cancelar e comparar estoque e saldo com os valores iniciais. |

Os arquivos `vitafortis-qa-valido.csv` e `vitafortis-qa-previa.xlsx` foram localizados no workspace pelos [links do roteiro](docs/reteste-candidato-2026-11-05.md#próxima-rodada-no-render--06112026). O relato anterior de adição de estoque aprovada com SHA desconhecido e a falha relatada no cadastro de conta continuam registrados separadamente na tabela anterior. Os testes locais, incluindo recuperação com SMTP em loopback, permanecem como evidência local anterior.

Nenhuma gravação foi iniciada. Não houve captura de estado inicial por cenário, restauração ou resíduo novo. Para retomar, é preciso comprovar o banco dedicado e sua rota de restauração e obter HTTP 200 com o SHA esperado, pagamento simulado e os assets carregados. Metas, indicadores, endereço e horário de retirada e prazo para BH/região seguem em aberto com o responsável.

#### Nova tentativa no Render — 07/11/2026

- **Timeout inicial reproduzido.** Em IPv4, o GET de `/api/v1/loja/versao` estabeleceu TCP e TLS em 49 ms e expirou após 35 s sem receber o primeiro byte (`curl` 28). O GET da página inicial respondeu HTTP 200 em 34,61 s. A causa da demora no servidor ou no caminho até ele ficou indeterminada.
- **HTTP e pagamento passaram, SHA falhou.** Na repetição, o GET de versão respondeu HTTP 200 em 0,41 s, com `pagamento: SIMULADO` e SHA `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`. O esperado era `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`.
- **Assets do HTML servido passaram por HTTP.** `/assets/index-BVBiYByX.js` retornou HTTP 200, `text/javascript` e 401.973 bytes. `/assets/index-C9mIINJx.css` retornou HTTP 200, `text/css` e 95.751 bytes. A checagem foi via terminal Windows, sem execução em navegador.
- **Reteste funcional bloqueado.** A divergência de SHA aciona a interrupção solicitada. O isolamento do banco e a rota de restauração seguem sem comprovação. Os estados funcionais da tabela acima permanecem válidos, agora com o SHA público identificado nesta nova tentativa. Nenhuma sessão de perfil foi aberta e nenhum dado foi gravado.

#### Conferência autenticada e checagem pública — 07/11/2026

- **Destino conferido em leitura.** O projeto visível no workspace do Render lista somente `Vita_Fortis_Backend` no ambiente `Production`. As configurações mostram branch `main`, Auto-Deploy `On Commit`, instância Free e health check em `/`. Deploys e Events mostram o deploy `94d09df` como ativo, disparado automaticamente, com duração de 4 min 31 s. O Git remoto confirma `main` em `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e `qa/vitafortis-2026-11-05` em `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`.
- **Demora anterior sem causa confirmada.** A janela de logs consultada mostrou atividade SQL no thread `main` às 03:55:37 (Brasília), compatível com inicialização. A leitura não confirmou o começo e o fim do startup nem vinculou a requisição expirada a essa atividade. A [hibernação do plano Free](https://render.com/docs/free#spinning-down-on-idle) é uma hipótese compatível com a demora seguida de resposta rápida.
- **Checagem pública, Windows desktop, perfil anônimo.** GET `/api/v1/loja/versao` passou em HTTP 200 e pagamento `SIMULADO` (0,202 s total, TLS em 0,053 s, primeiro byte em 0,202 s). A igualdade de SHA falhou: esperado `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`, servido `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`. GET `/`, `/assets/index-BVBiYByX.js` e `/assets/index-C9mIINJx.css` passaram em HTTP 200. O Chrome renderizou a loja e registrou ambos os assets como recursos carregados do HTML servido. A checagem de liberação do candidato segue bloqueada pela publicação pendente.
- **Publicação e restauração bloqueadas.** O responsável precisa identificar o serviço de QA e entregar evidência redigida do vínculo com o banco dedicado, da separação do banco real e de um ponto de restauração com procedimento já verificado. Confirmado o destino, é preciso disponibilizar uma rota autorizada por SHA completo que preserve o Auto-Deploy ou executar a publicação pelo responsável. O seletor manual observado anteriormente avisa que desativa o Auto-Deploy. A [API oficial](https://api-docs.render.com/reference/create-deploy) aceita `commitId` sem desativá-lo. Nenhum deploy, gravação ou restauração foi executado nesta rodada. Nenhum resíduo novo de QA foi criado.

Todos os cenários funcionais da matriz anterior permanecem **Não testado** no candidato, incluindo a atualização de SKU por `codigo` com variação apenas de maiúsculas e minúsculas, que deve manter o mesmo registro sem duplicá-lo. Recuperação permanece **Bloqueado** pela falta de caixa controlada e autorizada. Para a futura execução, o serviço observado é Free e o Render [bloqueia saída nas portas SMTP 25, 465 e 587 nesse plano](https://render.com/docs/free#other-limitations). Confirmar o transporte efetivo da rota de QA antes de testar o recebimento. A validação SMTP em loopback, os demais testes locais e o relato anterior do tester permanecem evidências separadas. Metas, indicadores, endereço e horário de retirada e prazo de BH/região seguem em aberto com o responsável.

#### Destino oficial de homologação e verificação de backup — 07/11/2026

- O proprietário confirmou `Vita_Fortis_Backend` em `https://vita-fortis-backend.onrender.com/` como destino oficial e autorizado de deploy e testes. A VitaFortis está em homologação e, segundo o proprietário, nunca foi publicada em produção. O rótulo `Production` e a branch `main` descrevem a configuração do Render. Esta confirmação resolve a identificação do destino solicitada nos registros anteriores.
- **Passou, leitura pública em Windows desktop.** GET `/api/v1/loja/versao` retornou HTTP 200, `SIMULADO` e primeiro byte em 0,441 s. GET `/` e os assets `index-BVBiYByX.js` e `index-C9mIINJx.css` também retornaram HTTP 200. **Falhou a igualdade de SHA:** observado `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc`, esperado `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`. O Git remoto reconfirmou os dois SHAs em `main` e `qa/vitafortis-2026-11-05`, respectivamente. O carregamento em Chrome do SHA anterior foi conferido na rodada acima.
- **Bloqueado, ponto de restauração pré-deploy.** O console da Aiven pediu login. A ligação do MySQL histórico ao `DB_URL` efetivo do Render e o estado atual dos backups do provedor ficaram sem verificação. Há `mysql.exe` e `mysqldump.exe` locais, scripts de backup e restauração no repositório e um dump histórico de 30/10 com SHA-256 conferido. O dump antecede as rodadas de QA posteriores e não representa o estado inicial atual. O responsável precisa autenticar a sessão do provedor ou fornecer acesso autorizado para comparar o alvo do Render em leitura, gerar ou identificar um ponto atual e ensaiar a restauração em um clone. O registro deve conter a data UTC e as verificações de integridade, sem credenciais ou strings de conexão.
- **Bloqueado, deploy do candidato.** A API oficial é a rota que preserva o Auto-Deploy; a sessão de execução não expôs credencial do Render e o seletor manual observado desativa essa opção. Após conferir o ponto de restauração, o responsável pode disparar o deploy pelo `commitId` na API ou disponibilizar acesso autorizado à mesma rota. Nenhum deploy, alteração de configuração, gravação de teste ou restauração ocorreu nesta rodada. A matriz funcional anterior continua aplicável ao candidato, com recuperação separada e bloqueada.

#### Banco da homologação confirmado no Aiven — 07/11/2026

- **Passou, identificação do banco.** Após o login do responsável, o console Aiven mostrou `vitafortis`, MySQL 8.4.11, `Running`, no plano `Free-1-1gb`. A comparação em leitura confirmou que host, porta e schema do `DB_URL` do Render correspondem aos valores do serviço Aiven. Valores de conexão omitidos.
- **Passou, existência de backup do provedor.** A aba Backups lista três cópias integrais. A mais recente exibida é de 06/11/2026 às 10:21:12 UTC, 289,6 MiB.
- **Bloqueado, ensaio de restauração.** Na sessão autenticada, `Fork service` está desabilitado. A existência do backup não comprova que ele pode ser restaurado para ensaio com o acesso atual. A alternativa lógica exige acesso MySQL autorizado para um dump atual e um alvo isolado compatível para verificar a restauração. Há clientes locais e um MySQL 8.0 ativo, mas a sessão não tem credencial de backup provisionada nem alvo MySQL 8.4 verificado. O responsável pode disponibilizar um clone restaurável no Aiven ou provisionar a rota de dump e restauração por canal seguro.
- **Deploy e testes funcionais seguem pendentes.** Nenhum deploy, fork, alteração de configuração ou gravação de teste ocorreu nesta rodada. Os estados por cenário seguem os da matriz anterior. A evidência nova resolve a identificação do banco, com o ensaio de restauração ainda pendente antes do deploy.

#### Pré-verificação da migração integral — 07/11/2026

- **Passou, leitura pública.** `/api/v1/loja/versao` retornou HTTP 200 com `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e `SIMULADO`. `git ls-remote` confirmou `main` nesse SHA e `qa/vitafortis-2026-11-05` em `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`.
- **Bloqueado, inventário e dump atual.** A origem foi identificada na conferência autenticada acima. A sessão de terminal não dispõe de credencial MySQL de backup provisionada. O dump histórico e o backup listado pelo provedor não substituem o inventário e a cópia atual. A verificação da restauração deve usar `COUNT(*)` por tabela e comparação de conteúdo. O `TABLE_ROWS` consultado pelo script existente é uma estimativa para InnoDB.
- **Bloqueado, restauração e troca.** Aguarda identificação ou autorização do novo serviço MySQL. Data e hash do dump atual, comparações por tabela e mudança de conexão permanecem pendentes. O SHA candidato não foi publicado e os cenários funcionais permanecem **Não testado** no banco novo. Recuperação por e-mail segue condicionada a caixa de QA autorizada e transporte funcional.

#### Acesso direto ao MySQL da homologação — 07/11/2026

- O responsável orientou continuar no MySQL `vitafortis` existente na Aiven. A conexão direta do cliente MySQL por TLS passou em uma consulta de versão. A tentativa de exportação integral falhou e o arquivo parcial foi descartado. A saída detalhada foi suprimida para proteger os dados de conexão, então a causa da falha permanece indeterminada. O próximo passo é repetir com diagnóstico redigido. A restauração e o deploy seguem pendentes.

#### Backup atual e restauração isolada — 07/11/2026

- **Passou, backup da homologação.** O diagnóstico redigido mostrou que o `mysqldump` rejeitava `--connect-timeout`. Após retirar essa opção, o dump por TLS terminou com sucesso. Captura iniciada às 22:33:43 de Brasília, 08/11 às 01:33:43 UTC, com 9.757.619 bytes e SHA-256 `30B5EDB7EEDC7EC063CC087AA9A51BB74A6DE5BBE28D565129EA3ECB21AA6FB9`. O SQL e o manifesto foram guardados fora do repositório em pasta local com acesso restrito.
- **Passou, restauração lógica isolada.** O SHA-256 foi reconferido antes da importação. O dump foi restaurado em uma instância local dedicada MySQL 8.0.44, em loopback. A reexportação confirmou 25 definições de tabela idênticas e 32 comandos `INSERT` com o mesmo SHA-256 `9C27B11C1C019ACBB0A1045E47D341269D37CEDC2F986E1FC425894D60883064`. O dump não continha definições de views, triggers, rotinas ou eventos. Foram contadas as linhas das 25 tabelas restauradas, incluindo 224 produtos, 41 registros de imagens, 12 pedidos, 10 usuários e 1 registro no histórico Flyway. A comparação usa o snapshot do dump como referência. A instância local foi desligada ao final. O ensaio usou MySQL 8.0, enquanto a origem usa 8.4.
- **Bloqueado, deploy por SHA com Auto-Deploy preservado.** A sessão continua sem acesso autenticado à API do Render. O candidato `d1d4a938183f6b93f26282fee9d1d37dfd500f5c` ainda aguarda publicação. Os testes de ponta a ponta e a limpeza dos dados de teste permanecem pendentes no candidato.

#### Deploy e testes públicos no Render — 08/11/2026

- **Passou, publicação.** Após autorização da CLI pelo responsável, o deploy por API `dep-dbnvnogu01pc73dsi8d0` ficou `live` às 01:39:23 de Brasília. A consulta ao serviço após a publicação confirmou branch `main`, Auto-Deploy ativo e gatilho `commit`. O SHA-256 do backup anterior ao deploy foi reconferido.
- **Passou, liberação.** GET `/api/v1/loja/versao` retornou HTTP 200, SHA `d1d4a938183f6b93f26282fee9d1d37dfd500f5c` e `SIMULADO`. No Edge, o HTML carregou `/assets/index-BEUD9Oml.js` e `/assets/index-C9mIINJx.css`, ambos HTTP 200.
- **Passou, catálogo público.** Edge desktop (1440 × 900) e celular emulado com toque (390 × 844). A API devolveu 220 itens sem filtro, 67 para `Whey` e 67 com `Whey` e preço máximo 100. A interface exibiu os totais correspondentes, voltou a 220 ao limpar e avançou para a página 2 de 19. O primeiro ensaio aguardava uma nova resposta de rede ao limpar, mas o total foi recuperado sem uma nova resposta. O reteste conferiu a interface ao limpar e a API ao paginar. A regressão de respostas fora de ordem, falhas obsoletas, filtros, limpeza e paginação também passou nos dois viewports com o bundle publicado e respostas da API interceptadas.
- **Pendências no candidato.** Cadastro e ajuste de estoque, importação CSV/XLSX, cadastro de cliente, permissões de colaboradores e Pix simulado seguem **Não testado** nesta rodada, aguardando as sessões fictícias. Recuperação segue **Bloqueado** até haver caixa de QA autorizada e transporte funcional. Os testes públicos não gravaram dados de teste.

#### Contas fictícias de homologação — 08/11/2026

- No SHA `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`, o ADMIN criou um colaborador pela API (ID 14, HTTP 201). O cadastro público criou um cliente pela API (ID 15, HTTP 201). Ambas as contas estavam ativas, fizeram login em sessões separadas (HTTP 200) e tiveram acesso à listagem ADMIN recusado (HTTP 403).
- As contas foram mantidas para a homologação e as senhas de QA foram guardadas localmente com proteção do usuário Windows. O cadastro pela interface continua **Não testado** nesta rodada. Os endereços fictícios não recebem e-mail e o cenário de recuperação segue **Bloqueado**.

#### Reteste de cadastro e fluxos autenticados — 08/11/2026

SHA publicado `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`, pagamento `SIMULADO`. A validação por navegador nesta rodada cobriu o cadastro. Os demais fluxos autenticados abaixo foram executados pela API com sessões separadas.

| Cenário | Estado | Perfil, dispositivo e evidência |
| --- | --- | --- |
| Cadastro público e login | Passou | Edge móvel emulado 390 × 844, CLIENTE. CPF inválido bloqueou o envio. CPF válido criou a conta fictícia ID 16 com HTTP 201, exibiu a confirmação e preencheu o e-mail no login. Login HTTP 200. |
| Produto inativo | Passou pela API | ADMIN, Windows desktop. SKU exclusivo criado com HTTP 201, ID 225. Leitura posterior confirmou R$ 12,50, estoque 2, situação inativa e ausência na busca pública. |
| Estoque e histórico | Passou pela API | ADMIN, Windows desktop. Ajuste de 2 para 5 com HTTP 200. Consulta posterior confirmou 5 e lançamento com anterior 2, novo 5, motivo, data e responsável. Saldo revertido a 2. |
| Importação XLSX misto | Passou pela API | ADMIN, Windows desktop. Prévia HTTP 200 com uma inserção prevista e uma rejeição. Confirmação HTTP 400. SKU válido ausente e quantidade de históricos igual antes e depois. |
| Importações CSV, XLSX corrigido e variação de caixa | Passou pela API | ADMIN, Windows desktop. IDs 226 e 227 inativos com estoques 3 e 4. Históricos 3 e 4 concluídos. Reimportação do código CSV em minúsculas classificou uma atualização, manteve o mesmo ID e não duplicou o SKU. |
| Colaboradores | Passou pela API | ADMIN e duas contas fictícias em sessões distintas, Windows desktop. Relatórios isolados por titular após crédito e cupom de QA. Área ADMIN recusada com HTTP 403. Revogação verificada em sessão aberta e nova com HTTP 403. Inativação recusou acesso da sessão anterior com HTTP 401 e novo login com HTTP 403. Papéis, atividade, permissões e saldos conferidos após restauração. |
| Pix simulado | Passou pela API | CLIENTE e ADMIN, Windows desktop. Primeira tentativa de revisão devolveu HTTP 400 com a sacola não preparada pelo roteiro. Após montar a sacola e aplicar o cupom, a revisão confirmou subtotal R$ 12,50, desconto R$ 1,25, total R$ 11,25 e cashback R$ 1,00. Reenvio HTTP 409 com um único pedido. Aprovação baixou uma unidade e creditou R$ 1,00. Cancelamento do pedido 13 recompôs estoque e saldo inicial. Cupom ID 4 e SKU de teste inativados, e sacola limpa. |
| Recuperação de senha | Bloqueado | Continua pendente a caixa autorizada e a confirmação do transporte no Render. Evidência SMTP em loopback permanece separada. |

Durante a primeira sequência autenticada, a rota de login retornou HTTP 429 com `Retry-After`. O reteste aguardou a janela e passou sem alteração de configuração. A suíte Maven executou 87 testes, com 0 falhas, 0 erros e 1 ignorado. Lint e build do frontend passaram. O build gerou os mesmos nomes de assets publicados. A falha de cadastro não foi reproduzida no navegador. Nenhuma correção de código ou republicação foi necessária nesta rodada.

Resíduos identificados no banco de homologação: conta fictícia ID 16, produtos inativos 225 a 227, históricos dos ajustes e das importações, cupom inativo ID 4 e pedido 13 cancelado com seus lançamentos de auditoria. A equivalência conferida refere-se aos estados operacionais de estoque, saldo e acesso. A rodada completa pela interface dos fluxos administrativos e de Pix continua pendente.

### Homologação pelo navegador — rodada de 08/11/2026

SHA da rodada: `d1d4a938183f6b93f26282fee9d1d37dfd500f5c`, HTTP 200 e pagamento `SIMULADO`. [Deploy ativo](https://dashboard.render.com/web/srv-dahmmkbm8hqs73cfrog0/deploys/dep-dbnvnogu01pc73dsi8d0). A CLI autenticada confirmou deploy `live`, serviço vinculado a `main` e Auto-Deploy com gatilho `commit`. No GitHub, `main` aponta para `94d09dfd5f8fae28612fda7a56bcbf1a1fd4cafc` e a branch de QA para o SHA da rodada. Após o cold start, as requisições voltaram a responder. Os primeiros timeouts de até 55 segundos ocorreram enquanto a página do Render mostrava inicialização.

O dump por TLS começou às 02:46:24 BRT, tem 9.768.916 bytes e SHA-256 `7A237F5494C36D907136A25A43AA306E9E8BD4A07DC84149DFC9239E84FA075B`. O alvo do Render coincide com o serviço Aiven confirmado, e os arquivos estão em pasta restrita fora do repositório. A restauração isolada em MySQL local 8.0.44 reexportou 25 definições e 32 comandos INSERT com hashes idênticos aos do dump. A instância foi desligada após a comparação. A origem usa MySQL 8.4.11. No provedor, o último backup exibido é de 07/11 às 10:21:10 UTC e `Fork service` está desabilitado. A restauração do clone verifica a cópia lógica. A reversão seletiva dos dados da nova rodada no Aiven ainda precisa ser confirmada para as ações que criam registros permanentes.

Matriz confrontada com `STATUS_CONSOLIDADO_V2.md` (revisão de 28/10), as rotas React e os contratos HTTP. Nas linhas executadas, o status refere-se ao método indicado. A evidência desta rodada de interface vem do DOM do Chrome. Os códigos HTTP explicitamente identificados como API foram obtidos em consultas separadas. A aba Rede não foi capturada nesta rodada. As evidências anteriores de API e da interface ficam nos registros acima.

| Cenário e perfil | Estado | Método e dispositivo | Passos e esperado | Observado, HTTP e evidência |
| --- | --- | --- | --- | --- |
| Versão e assets · Visitante | Passou | Chrome desktop e API | Abrir home, conferir bundle e versão exata | `/api/v1/loja/versao` HTTP 200, SHA da rodada, `SIMULADO`. JS `index-BEUD9Oml.js` e CSS `index-C9mIINJx.css` vistos como recursos no Chrome. Ambos HTTP 200 em consulta independente. |
| Home e navegação · Visitante | Passou | Chrome desktop | Abrir home e percorrer navegação e vitrines | Home renderizou hero, seleções, rodapé e links. GET da home HTTP 200 em consulta independente. |
| Catálogo · Visitante | Passou | Chrome desktop | Abrir catálogo e listar produtos e filtros | 220 produtos, página 1 de 19, filtros disponíveis. Código da requisição da interface não capturado. |
| Busca · Visitante | Passou | Chrome desktop e viewport solicitado 390 × 844 | Buscar Whey e conferir total | 67 itens, página 1 de 6. Código da requisição da interface não capturado. |
| Ordenação e preço · Visitante | Passou | Chrome desktop e viewport solicitado 390 × 844 | Selecionar menor preço e máximo 100 na busca Whey | URL com `PRECO_ASC` e `precoMax=100`, 67 itens e primeiro item Whey Homologacao no ensaio de viewport. Código da requisição da interface não capturado. |
| Limpeza e paginação · Visitante | Passou | Chrome desktop e viewport solicitado 390 × 844 | Limpar todos e avançar | 220 itens, página 2 de 19. Um primeiro clique no ensaio de viewport manteve a URL filtrada. O clique seguinte e a tentativa de reprodução passaram. Sem HTTP capturado, ocorrência inconclusiva. |
| Resposta de busca atrasada · Visitante | Passou | Edge automatizado, desktop e móvel emulado | Reter resposta anterior, enviar consulta nova, liberar resposta antiga e preservar resultado atual | `pnpm.cmd test:catalog` passou nos dois perfis contra o bundle publicado, com respostas de catálogo interceptadas. Sem HTTP real para as respostas interceptadas. |
| Categorias · Visitante | Passou | Chrome desktop e API | Selecionar Proteínas e filtrar | 71 resultados. HTTP 200 em GET independente de categorias. O seletor também contém `CARBOIDRATOS` e `CARBOIDRATROS`. A taxonomia exige conferência antes de alterar cadastros existentes. |
| Marcas · Visitante | Passou | Chrome desktop e API | Abrir Marcas e filtrar FTW no catálogo | Página abriu, filtro exibiu 178 resultados. GET independente de marcas HTTP 200. |
| Objetivos · Visitante | Passou | Chrome desktop e API | Abrir Objetivos e filtrar Performance no catálogo | Página abriu, filtro exibiu 35 resultados. GET independente de objetivos HTTP 200. |
| Ofertas vazias · Visitante | Passou | Chrome desktop | Abrir Ofertas e exibir estado vazio quando não há itens | “Nenhuma oferta disponível no momento.” HTTP da requisição da interface não capturado. |
| Oferta com itens · Visitante | Não testado | Chrome desktop previsto | Abrir oferta elegível e conferir preço e desconto | Cenário positivo aguarda produto de QA em oferta. |
| Novidades · Visitante | Passou | Chrome desktop | Abrir Novidades e listar lançamentos | Vitrine exibiu cards de lançamento e link com `lancamento=true`. HTTP da requisição da interface não capturado. |
| Kits vazios · Visitante | Passou | Chrome desktop | Abrir Kits e exibir estado vazio quando não há itens | “Nenhum kit disponível no momento.” HTTP da requisição da interface não capturado. |
| Kit com itens · Visitante | Não testado | Chrome desktop previsto | Abrir kit elegível e conferir detalhe | Cenário positivo aguarda kit de QA. |
| Detalhe e imagem · Visitante | Passou | Chrome desktop e API | Abrir card da página 2 e confirmar detalhe, imagem e avaliações | Detalhe de Whey Refil Chocolate, imagem principal carregada e seção de avaliações exibida. GET independente de outro produto, ID 29, HTTP 200. |
| Galeria com várias imagens · Visitante | Não testado | Chrome desktop e móvel previstos | Alternar miniaturas e conferir imagem correspondente | A amostra inspecionada tinha uma imagem principal. |
| Acesso anônimo · Visitante | Passou | Chrome desktop | Abrir `/admin`, `/pedidos` e `/colaborador` sem autenticação | As três rotas exibiram login. Código da requisição da interface não capturado. |
| CPF inválido · Cliente | Não testado | Chrome desktop/móvel previstos | Digitar CPF inválido e confirmar ausência de POST | Evidência anterior de interface em viewport móvel no mesmo SHA. |
| E-mail duplicado · Cliente | Não testado | Chrome desktop/móvel previstos | Submeter e-mail de QA já cadastrado e rejeitar duplicidade | Sem execução nesta rodada. |
| Cadastro válido · Cliente | Não testado | Chrome desktop/móvel previstos | Criar conta fictícia, recarregar e autenticar | Evidência anterior de interface no mesmo SHA para conta ID 16, cadastro HTTP 201. |
| Login e perfil · Cliente e colaborador | Passou | API, sessões independentes no Windows | Reutilizar credenciais protegidas, autenticar e consultar `/me` | Login e perfil HTTP 200 para IDs 14, 15 e 16. Interface autenticada pendente nesta rodada. |
| Acesso administrativo indevido · Cliente e colaborador | Passou | API, sessões independentes no Windows | Consultar lista de usuários do ADMIN com perfis sem permissão | HTTP 403 para IDs 14, 15 e 16. |
| Logout e invalidação de sessão · Cliente e colaborador | Passou | API, sessões independentes no Windows | Sair e recarregar perfil com a sessão anterior | Logout HTTP 204 e perfil posterior HTTP 403 para IDs 14, 15 e 16. |
| Leitura e isolamento de endereços · Dois clientes | Passou | API, sessões independentes no Windows | Ler endereços próprios e trocar o ID pelo outro titular | HTTP 200 para o próprio ID e 403 para o outro nas duas direções, IDs 15 e 16. |
| Leitura e isolamento de favoritos · Dois clientes | Passou | API, sessões independentes no Windows | Ler favoritos próprios e trocar o ID pelo outro titular | HTTP 200 para o próprio ID e 403 para o outro nas duas direções. |
| Leitura e isolamento de pedidos · Dois clientes | Passou | API, sessões independentes no Windows | Ler pedidos próprios e trocar o ID pelo outro titular | HTTP 200 para o próprio ID e 403 para o outro nas duas direções. |
| Resumo e relatório sem permissão · Colaborador e clientes | Passou | API, sessões independentes no Windows | Ver resumo como colaborador e negar relatório sem permissão e resumo ao cliente | ID 14 recebeu 200 no resumo e 403 no relatório. Clientes 15 e 16 receberam 403 no resumo. |
| Edição de perfil · Cliente | Não testado | Chrome desktop previsto | Alterar campo fictício, recarregar, conferir e reverter | Sem execução nesta rodada. |
| Endereços · Cliente | Não testado | Chrome desktop previsto | Criar, editar, selecionar principal e excluir endereço fictício | Sem execução das ações pela interface nesta rodada. |
| Favoritos · Cliente | Não testado | Chrome desktop previsto | Favoritar, recarregar, remover e conferir persistência | Sem execução das ações pela interface nesta rodada. |
| Pedidos próprios · Cliente, RF10 | Não testado | Chrome desktop previsto | Abrir lista, detalhe e linha do tempo de pedido próprio | Sem execução pela interface nesta rodada. |
| Privacidade · Cliente, RCP-02 | Não testado | Chrome desktop previsto | Alterar consentimento, solicitar acesso, correção e exclusão, verificar isolamento | Sem execução nesta rodada. |
| Recuperação · Cliente, RF07 | Bloqueado | Chrome e caixa de QA previstos | Receber link, redefinir, autenticar com a nova senha e rejeitar senha antiga e token usado | Falta caixa receptora de QA autorizada. Render tem e-mail habilitado na porta 2525. Entrega e redefinição remotas não verificadas. |
| Novo produto inativo · ADMIN | Não testado | Chrome desktop previsto | Criar por R$ 12,50, estoque 2, recarregar e negar visibilidade pública | Execução anterior somente pela API no mesmo SHA. |
| Estoque e histórico · ADMIN, RN03 | Não testado | Chrome desktop previsto | Ajustar 2 → 5 com motivo e conferir anterior, novo, data e responsável | Execução anterior somente pela API no mesmo SHA. |
| Edição · ADMIN | Não testado | Chrome desktop previsto | Editar produto e conferir todos os campos depois de recarregar | Sem execução nesta rodada. |
| Ativação e inativação · ADMIN, RF28 | Não testado | Chrome desktop previsto | Ativar, conferir visibilidade pública, inativar e conferir ausência | Sem execução pela interface nesta rodada. |
| Imagens · ADMIN | Não testado | Chrome desktop previsto | Enviar imagem, reordenar galeria e conferir persistência | Sem execução nesta rodada. |
| Cadastros auxiliares · ADMIN, RF30 | Não testado | Chrome desktop previsto | Criar, editar e inativar categoria e atributo de QA, rejeitar duplicidade | Sem execução nesta rodada. |
| Prévia XLSX misto · ADMIN, RF33–RF35 | Não testado | Chrome desktop previsto | Prever uma inserção e rejeitar estoque −1. Confirmar que nenhum registro ou histórico foi gravado | Execução anterior somente pela API no mesmo SHA. |
| CSV válido · ADMIN, RF33–RF35 | Não testado | Chrome desktop previsto | Importar com SKU exclusivo, estoque 3 e inativo. Conferir histórico concluído | Execução anterior somente pela API no mesmo SHA. |
| XLSX corrigido · ADMIN, RF33–RF35 | Não testado | Chrome desktop previsto | Importar sem a linha inválida, estoque 4 e inativo. Conferir histórico concluído | Execução anterior somente pela API no mesmo SHA. |
| Reimportação por caixa · ADMIN, RF33–RF35 | Não testado | Chrome desktop previsto | Variar maiúsculas/minúsculas do SKU e manter o mesmo ID | Execução anterior somente pela API no mesmo SHA. |
| Usuários, cadastro e papéis · ADMIN | Não testado | Chrome desktop previsto | Criar perfil fictício, alterar papel, recarregar e restaurar | Sem execução pela interface nesta rodada. |
| Extrato e cupons próprios · Dois colaboradores, RF22/RF25 | Não testado | Duas sessões de Chrome previstas | Exibir somente registros próprios e negar registros do outro titular | Execução anterior somente pela API no mesmo SHA. |
| Revogação de relatório · ADMIN e colaborador | Não testado | Chrome desktop, sessão aberta e nova previstas | Revogar, negar acesso em ambas as sessões e restaurar permissão | Execução anterior somente pela API no mesmo SHA. |
| Inativação e reativação · ADMIN e colaborador | Não testado | Chrome desktop, sessões independentes previstas | Invalidar sessão anterior, negar login e restaurar atividade | Execução anterior somente pela API no mesmo SHA. |
| Destino do cashback · Colaborador, RN07 | Não testado | Chrome desktop previsto | Alternar Pix/desconto, recarregar e restaurar preferência | Sem execução nesta rodada. |
| Sacola · Cliente | Não testado | Chrome desktop previsto | Adicionar SKU exclusivo, alterar quantidade, validar limite de estoque e recarregar | Sem execução nesta rodada. |
| Cupons e benefícios · ADMIN e cliente, RNF04 | Não testado | Chrome desktop previsto | Gerir cupom fixo/percentual, testar validade, mínimo, limite e cupom ligado ao titular | Evidência anterior de API para um cupom de colaborador. |
| Retirada · Cliente, RF15/RF18 | Não testado | Chrome desktop previsto | Revisar frete zero, prazo e subtotal/desconto/total, conferir disponibilidade após aprovação | Endereço e horário aguardam decisão do responsável. |
| Entrega · Cliente, RF16/RF18 | Não testado | Chrome desktop previsto | Testar estado atendido/não atendido, total e limite estrito acima de R$ 200 após desconto | Prazo detalhado para BH e região aguarda decisão do responsável. |
| Pix e reenvio · Cliente, RF17/RF23/RN05 | Não testado | Chrome desktop previsto | Conferir revisão, cashback sobre subtotal e pedido único | Execução anterior somente pela API no mesmo SHA. |
| Aprovar Pix, baixa e crédito únicos · ADMIN, RN03 | Não testado | Chrome desktop previsto | Aprovar duas vezes e conferir uma baixa e um crédito | Execução anterior somente pela API no mesmo SHA. |
| Cancelamento, estorno e sacola · ADMIN e cliente, RN03 | Não testado | Chrome desktop previsto | Cancelar, conferir estoque, saldo, auditoria e sacola | Execução anterior somente pela API no mesmo SHA. |
| Recusar pagamento · ADMIN, RF17 | Não testado | Chrome desktop previsto | Recusar pagamento simulado pendente e conferir ausência de baixa e crédito | Sem execução nesta rodada. |
| Estados e filtros de pedidos · ADMIN, RF36 | Não testado | Chrome desktop previsto | Testar separação, disponível para retirada, enviado, entregue, filtros e transições inválidas | Sem execução nesta rodada. |
| Avaliação e moderação · Cliente e ADMIN | Não testado | Chrome desktop previsto | Criar avaliação fictícia, moderar e confirmar visibilidade conforme aprovação | Seção pública vazia observada na amostra. |
| Tratamento de privacidade · ADMIN, RCP-02 | Não testado | Chrome desktop previsto | Filtrar, tratar solicitação de QA e conferir mudança persistente e auditoria | Sem execução nesta rodada. |
| Métricas e relatórios · ADMIN, RF38–RF40 | Não testado | Chrome desktop previsto | Conferir vendas aprovadas, cupons, estoque baixo, período e acesso restrito | Sem execução nesta rodada. |
| Exportações · ADMIN, RF38–RF40 | Não testado | Chrome desktop previsto | Exportar CSV, XLSX e PDF e conferir linhas e filtros | Sem execução nesta rodada. |
| Metas e indicadores · Colaborador | Bloqueado | Requisito | Definir fonte, período, fórmula, visibilidade e efeito de cancelamentos | Pendente decisão do responsável. |
| E-mails transacionais · Cliente, RF19 | Bloqueado | E-mail de QA previsto | Verificar recebimento por evento de pedido | Falta caixa de QA autorizada. |
| Reversão da rodada · ADMIN/Aiven | Não testado | Banco e interface previstos | Reverter dados de QA sem afetar registros alheios e inventariar resíduos de auditoria | Esta rodada não alterou dados funcionais de QA. Os resíduos anteriores permanecem registrados acima. |

Nenhum defeito de código foi reproduzido de forma conclusiva. Não houve commit, push, novo deploy ou novo resíduo funcional de QA. Para continuar a rodada autenticada pela interface, foram localizadas credenciais de QA utilizáveis para dois clientes e um colaborador. Faltou confirmar uma sessão ADMIN e uma segunda sessão de colaborador com credenciais de QA disponíveis para o navegador. Para os cenários que deixam cadastros ou auditoria, o próximo passo é validar a compensação seletiva por ID e os estados inicial e final na base existente.
