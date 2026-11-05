# Backlog técnico priorizado

Fonte funcional: `STATUS_CONSOLIDADO_V2.md`, revisado em 08/10/2026. Este documento organiza trabalho técnico; não substitui a matriz.

| ID | Prioridade | Objetivo e estado atual | Evidência / verificação necessária | Responsável | Dependências, risco e aceite | Aprovação do dono |
| --- | --- | --- | --- | --- | --- | --- |
| TEC-01 | P0 | Validação local reexecutada em 09/10/2026: 66 testes Java, lint e build React aprovados na árvore local. | Preservar os resumos e repetir sobre o commit que vier a ser candidato à publicação. | `qa-tester` | Evidência local aprovada; ainda não equivale a commit, publicação ou homologação pública. | Não. |
| TEC-02 | P0 | Revisar CSRF local sem publicar. Implementação está em alterações locais. | Conferir sessão, token, login/logout, recuperação e operações mutáveis; executar testes direcionados. | `security-reviewer` + `backend-java` | Risco alto de regressão de autenticação. Aceite: rejeição sem token, aceitação válida e isolamento preservado. | Não para revisão; sim para publicar. |
| TEC-03 | P0 | Corrigido tecnicamente no ambiente local em 09/10/2026. O build React atual foi sincronizado sem excluir bundles históricos; `static/index.html` aponta para `index-CtONrlfO.js`, que contém o fluxo CSRF. | Lint/build, 66 testes Java, comparação integral `dist`/`static` e pacote Spring aprovados; repetir no commit candidato e homologar publicamente somente após autorização. | `frontend-react` + `devops-release` + `qa-tester` + `security-reviewer` | O JAR local incorporou os mesmos hashes do build. Ativos antigos permanecem preservados para inventário/remoção futura. | Exclusão/publicação continuam em etapa separada. |
| TEC-04 | P1 | Validar fluxos ADMIN, CLIENTE, COLABORADOR e RCP-02 em ambiente seguro. | Preparar dados isolados e executar roteiro autenticado sem produção. | `qa-tester` | Risco médio; depende de ambiente/test data. Aceite: evidências por perfil e falhas registradas. | Não. |
| TEC-05 | P1 | Resolver estratégia Flyway. O perfil padrão usa `ddl-auto=update` e Flyway desativado; `application-prod.properties` usa `validate`, Flyway ativo e baseline 0. `render.yaml` e o Dockerfile não ativam explicitamente `prod`. | Confirmar o perfil efetivo do ambiente, inventariar `flyway_schema_history`, verificar duplicidade com V1/V2 e ensaiar migrate + Hibernate validate em clone MySQL descartável. | `database` | Risco alto: esquema já criado pelo Hibernate pode colidir com V1/V2; H2 não prova MySQL/Linux. Aceite: ensaio repetível e proposta de rollout/rollback, sem produção. | Não para ensaio; sim antes de produção. |
| TEC-06 | P1 | Ensaiar backup/restauração isolada. Ainda sem evidência registrada. | Restaurar cópia sanitizada em banco isolado, comparar contagens e integridade. | `database` | Risco alto se alvo incorreto. Aceite: alvo isolado comprovado, restauração e checagens documentadas. | Não no isolado; sim para dados/produção reais. |
| TEC-07 | P2 | Criar matriz de compatibilidade. Evidência atual é apenas inspeção histórica. | Definir navegadores/dispositivos e registrar fluxos e resultados por versão. | `qa-tester` + `frontend-react` | Risco baixo. Aceite: matriz com ambiente, data, commit e resultado. | Não. |
| TEC-08 | P2 | Preparar homologação pública futura. Publicação confirmada de 22/09 não contém prova do CSRF local de 29/09. | Checklist com commit alvo, deploy autorizado, health/smoke, perfis e LGPD. | `devops-release` | Risco alto de alegação incorreta de release. Aceite: plano pronto; execução só após autorização. | Sim para deploy e testes públicos autenticados. |
| TEC-09 | P2 | Validar RF07 local e preparar contrato SMTP. | Testar token/fluxo com transporte simulado; separar envio real. | `backend-java` + `qa-tester` | Risco médio. Aceite: fluxo local comprovado, sem credencial real. | SMTP real, remetente e templates dependem do dono. |
| TEC-10 | P2 | Corrigir documentação de variáveis de banco. O README cita `JDBC_URL`, `DB_USER` e `DB_PASS`, mas a aplicação e `render.yaml` usam `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. | Confirmar os nomes efetivos sem expor valores e alinhar somente a documentação. | `devops-release` | Risco médio de configuração incorreta. Aceite: README coerente com os placeholders reais e revisão sem credenciais. | Não. |

## Retorno do tester de 05/11/2026

Triagem por leitura do commit local `0ac0668` e da [matriz funcional revisada em 28/10/2026](../STATUS_CONSOLIDADO_V2.md). O ambiente, a versão e o aparelho usados pelo tester ainda precisam ser identificados. Todos os itens estão abertos.

| ID | Prioridade | Área e evidência | Critério de aceite |
| --- | --- | --- | --- |
| QA-01 | P0 | **Criar conta.** O cadastro público tem envio e mensagens no código. O clique sem resposta precisa ser reproduzido. Distinguir cadastro público de criação de colaborador no painel. | Cadastro válido cria uma conta e confirma o acesso. Dados inválidos, duplicidade e falha de rede recebem mensagens claras. Validar também o formulário administrativo com o perfil correto. |
| QA-02 | P0 | **Checkout (RF15/RF16/RF18 e RN03).** Há revisão de valores e pagamento simulado. O commit local contém correções de reconciliação e testes específicos. | Percorrer do produto à confirmação por entrega e retirada. Validar cupom, frete, mudança de preço ou estoque, envio repetido, pedido no painel e baixa de estoque na aprovação simulada. Pagamento real depende da escolha do gateway. |
| QA-03 | P0 | **Responsividade e erros.** O relato é transversal. Priorizar cadastro, compra e operação administrativa. | Loja e painel utilizáveis no celular e desktop. Campos, tabelas, modais e ações críticas acessíveis. Falhas de validação, rede e sessão expirada indicam como seguir e preservam dados quando possível. |
| QA-04 | P1 | **Estoque.** A API permite ajuste com motivo e histórico. A tela Produtos não expõe o ajuste. | ADMIN vê o saldo, ajusta com motivo e confere saldo e histórico com responsável. Validar permissões e saldo inválido. |
| QA-05 | P1 | **Categorias e atributos (RF28/RF30).** Formulário e lista permitem inativar. A operação preserva vínculos. Localizar o texto incompleto no ambiente testado. | Textos completos no celular, inativação na lista e reativação clara. Definir a política de exclusão considerando cadastros vinculados a produtos. |
| QA-06 | P1 | **Produtos e planilha (RF33–RF35).** Já há modelo CSV, prévia e histórico. O controle citado no fim do feedback precisa ser identificado. | Explicar como baixar, preencher, importar, revisar e confirmar. Mostrar erro por linha quando disponível. Identificar o controle citado por captura ou reprodução antes de alterar. |
| QA-07 | P1 | **Destaques.** O painel grava a seleção, mas a home preenche a seção com outros produtos quando não há seleção. | Marcar e desmarcar altera a vitrine. A seção mostra os produtos ativos escolhidos e tem estado definido para seleção vazia. |
| QA-08 | P2 | **Busca em Usuários.** A listagem administrativa atual não tem busca. | Buscar por nome ou e-mail, limpar o filtro e informar quando não houver resultado. Validar com ADMIN. |
| QA-09 | P2 | **Galeria.** O cadastro e a página pública usam uma única imagem. | Adicionar, remover e ordenar imagens, escolher a capa e navegar no celular. Preservar a imagem atual dos produtos existentes e validar a migração em banco isolado. |

**Ordem de execução.** Identificar a versão testada e reproduzir os fluxos em ambiente isolado. Corrigir P0, incorporar os mesmos critérios de responsividade e erros nas entregas P1 e então avançar para P2.

**Reteste.** Engenharia entrega um commit identificado e valida a mudança com testes direcionados, lint e build conforme o escopo. QA percorre o fluxo no navegador e registra ambiente, versão, perfil, passos, resultado e evidência. O tester recebe um candidato após a validação interna dos fluxos essenciais. Publicação depende de autorização explícita.

### Evidência local de 05/11/2026

QA-01 a QA-05 e QA-07 a QA-09 tiveram os fluxos registrados no Edge local. QA-06 passou no fluxo CSV de modelo, prévia, correção e confirmação. O controle de Produtos mencionado sem identificação continua pendente de captura ou passos. A validação em aparelhos físicos permanece pendente. Consulte a [matriz de evidências](evidencias-qa-2026-11-05.md) e o [guia de operação](guia-operacao-qa.md). O Render foi inspecionado somente em leitura e servia o commit `0ac066878cd3d2b896b499cc0392de6c30d7432f`.

## Rodada funcional dos três perfis — 05/11/2026

A [matriz por perfil e funcionalidade](homologacao-funcional-2026-11-05.md) registra a execução, as negativas e os cenários pendentes. A versão pública em leitura continua em `0ac066878cd3d2b896b499cc0392de6c30d7432f`; a rodada local partiu de `e2374dde8c2fc5b333ba5c96d4236dd32a91c8a6`.

| ID | Evidência nova | Acompanhamento |
| --- | --- | --- |
| QA-10 | A tabela de privacidade provocava 757 px de largura em viewport de 390 px. | Corrigida a largura mínima da tabela de Minha conta. |
| QA-11 | Para subtotal de R$ 149,90, desconto de R$ 14,99 e cashback de 5%, a revisão anunciava R$ 6,75 e o crédito era de R$ 7,50. | Sacola, revisão e crédito agora usam a mesma base prevista em RF23/RN05. |
| QA-12 | COLABORADOR inativado conseguia consultar cashback em sessão aberta. | Aplicada conferência de situação e papel da conta na sessão autenticada. |
| QA-13 | A permissão de relatórios chega à sessão renovada. O COLABORADOR tem saldo e destino de cashback, mas a área não oferece relatório, cupons vinculados, metas ou indicadores próprios. | Confirmar o escopo dessas funções antes da liberação integral. |

O parecer e as evidências de reteste estão vinculados à matriz. Permanecem pendentes o aparelho físico, a entrega real de e-mail, as decisões comerciais e jurídicas e o aceite público do mesmo pacote.

| ID | Evidência complementar | Acompanhamento |
| --- | --- | --- |
| QA-14 | Em viewport de 390 px, Remover interceptava Aumentar na sacola. | Os controles agora ocupam espaços separados. |
| QA-15 | Após redefinir a senha, a rota voltava a `/entrar` e mantinha o formulário de redefinição. | A transição agora volta ao formulário de login. |
