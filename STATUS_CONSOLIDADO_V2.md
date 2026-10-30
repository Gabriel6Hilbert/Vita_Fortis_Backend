# Vita Fortis V2 - matriz consolidada de status

Revisado em: 28/10/2026
Evidencia tecnica mais recente: regras comerciais do commit funcional `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4` publicadas pelo commit documental `c72100a1d8ec4015c53d4bf65b4ed079781c6c54`, com HTTP 200, SHA e hashes confirmados em 26/10/2026.

Este e o documento canonico de acompanhamento do projeto. Em caso de divergencia com relatorios ou guias anteriores, prevalece esta matriz, desde que nenhuma mudanca posterior de codigo invalide as evidencias registradas aqui.

## Como interpretar os estados

- **Concluido**: implementado no codigo atual e coberto pela validacao tecnica disponivel.
- **Parcial tecnico**: existe implementacao interna, mas ainda falta prova operacional, configuracao de ambiente ou ampliacao de validacao que pode ser feita pela equipe tecnica.
- **Depende do dono**: exige decisao comercial ou juridica, conteudo aprovado, credencial, conta de fornecedor, preco ou estoque que a equipe nao deve inventar.

## Matriz unica

| Requisito ou area | Estado | Situacao comprovada | O que ainda falta |
| --- | --- | --- | --- |
| RF10 - pedidos do cliente | Concluido | Cliente consulta somente os proprios pedidos, detalhes, status e linha do tempo persistida. | Nenhuma pendencia funcional conhecida. |
| RN03 - estoque e pagamento | Concluido | A baixa definitiva ocorre na primeira aprovacao do pagamento; cancelamento recompoe o estoque quando aplicavel e repeticao da aprovacao e protegida. | Nenhuma validacao do dono e necessaria para manter a regra implementada. |
| Movimentacoes de estoque | Concluido | Ajustes possuem historico persistente, motivo e responsavel nos fluxos administrativos implementados. | Manter cobertura de regressao nas proximas alteracoes. |
| RN07 - destino do cashback | Concluido | Colaborador escolhe Pix ou desconto; a escolha fica persistida e aparece na API e na interface. | Nenhuma pendencia funcional conhecida. |
| RF22/RF25 - privacidade do colaborador | Concluido | Area e APIs limitam dados, protegem detalhes de clientes e respeitam autorizacao por perfil. | Nenhuma pendencia funcional conhecida. |
| RF23/RN05 - calculo do cashback | Concluido | Usa o percentual persistido do cupom sobre o total de produtos. Essa e uma decisao de implementacao porque a fonte nao fixa toda a composicao da base. | Alteracao da formula somente se o dono aprovar outra regra comercial. |
| RF15/RF18 - retirada e revisao | Concluido tecnicamente | Retirada sem frete, preparacao em ate 24 horas, estado `DISPONIVEL_RETIRADA` e revisao de subtotal, desconto, frete, prazo e total foram versionados em `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4` e publicados pelo commit `c72100a1d8ec4015c53d4bf65b4ed079781c6c54`. | Endereco e horario de retirada dependem do gerente; repetir o fluxo completo do checkout no ambiente publico. |
| RF16 - frete e entrega propria | Concluido tecnicamente | A Vita Fortis fara a propria entrega, sem transportadora externa. O backend atende MG, SP, RJ e ES; frete gratis usa comparacao estrita `> 200` sobre produtos apos desconto; abaixo do limite permanece a tarifa configurada no sistema. O ADMIN possui handoff manual para WhatsApp com mensagem pronta. | Confirmar no ambiente real o numero do cliente e o fluxo operacional; prazo detalhado por municipio pode ser refinado depois. Nao ha rastreio de transportadora. |
| RF28/RF30 - produtos, categorias e atributos | Concluido | Inativacao logica de produtos e cadastros persistentes de categorias e atributos. | Preco e estoque dos produtos FTW importados dependem do dono antes da ativacao. |
| RNF04 - cupons | Concluido | Desconto fixo ou percentual, validade, valor minimo, limite de usos e protecao concorrente. | Nenhuma pendencia funcional conhecida. |
| RF33-RF35 - importacao | Concluido | Previa CSV/XLSX, validacao integral, transacao e historico persistente com arquivo, hash, data, responsavel e resultado. | Nenhuma pendencia funcional conhecida. |
| RF36 - consulta de pedidos | Concluido | Filtros combinaveis usam dados persistidos no fluxo banco, backend, API e frontend. | Nenhuma pendencia funcional conhecida. |
| RF38-RF40 - indicadores e relatorios | Concluido | Filtros administrativos, vendas aprovadas, cupons, estoque baixo e exportacoes CSV/XLSX/PDF. | Nenhuma pendencia funcional conhecida. |
| RF07 - recuperacao de senha | Parcial tecnico | Token seguro, expiracao, uso unico, endpoints, telas e modelo transacional centralizado estao implementados. | Criar e autenticar a conta Brevo, cadastrar segredos no Render e testar entrega real. |
| RCP-02 - consentimento e direitos do titular | Concluido | Consentimento e solicitacoes de acesso, correcao e exclusao possuem isolamento por usuario, tratamento administrativo, retencao e auditoria. | Executar novamente o fluxo completo no ambiente publico na proxima homologacao. |
| Seguranca de sessao e CSRF | Concluido | A protecao vinculada a sessao foi homologada publicamente no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`: emissao de token e cookie, rejeicao sem token ou com token invalido, aceitacao com token valido, login CLIENTE, logout autenticado e recuperacao de senha foram comprovados. | Manter regressao nas proximas alteracoes de autenticacao e nas novas operacoes mutaveis. |
| RCP-01 - base legal e inventario | Parcial tecnico | Inventario criado com controlador, tratamentos, finalidades, bases propostas e tabela operacional de retencao. Tokens expirados passam a ser excluidos automaticamente. | Juridico deve validar bases e prazos, confirmar operadores/transferencias e a regra para menores. |
| RCP-03 - politicas | Parcial tecnico | Minutas separadas foram estruturadas e a politica de privacidade recebeu os prazos propostos de retencao. | Decidir tecnologias de publicidade, revisar juridicamente e autorizar publicacao. |
| RF17 - pagamento | Parcial tecnico | Novos pedidos apresentam e aceitam Pix ou cartao; a conciliacao simulada permanece restrita ao ADMIN, sem armazenar numero do cartao ou CVV. | Pagamento real permanece pendente: escolher gateway, criar conta, fornecer credenciais e definir parcelamento, juros, validade do Pix, estorno e webhook. |
| RF19 - e-mails transacionais | Parcial tecnico | Brevo foi definido como relay SMTP. Recuperacao de senha e atualizacoes de pedido usam modelos simples; eventos de pedido sao processados apos o commit e falha de e-mail nao desfaz a compra. | Criar conta, verificar remetente e dominio com DKIM/DMARC, cadastrar segredos no Render e testar Gmail/Outlook. |
| Produtos FTW importados | Concluido como fluxo operacional | Produtos importados permanecem inativos, sem preco e com estoque zero. A equipe da Vita Fortis define preco/estoque e ativa somente o que estiver disponivel. | Cadastro operacional continuo da loja; nao bloqueia o encerramento tecnico. |
| Migracoes de banco | Parcial tecnico | O perfil `prod` foi endurecido. Em banco isolado, V1/V2 foram aplicadas, validadas e a segunda execucao aplicou zero migracoes; o baseline 2 de esquema legado integral tambem foi ensaiado. | Repetir no clone MySQL da mesma versao da producao, apos inventario e backup real; so depois ativar `SPRING_PROFILES_ACTIVE=prod`. |
| Homologacao publica | Parcial tecnico | Em 26/10/2026 o commit `c72100a1d8ec4015c53d4bf65b4ed079781c6c54` foi comprovado com HTTP 200, SHA completo, `index-BBO_9H0d.js`, CSS com hashes correspondentes, 181 produtos ativos e pagamento simulado. O CSRF tambem foi homologado publicamente. | Homologar o checkout comercial completo, ADMIN, COLABORADOR e RCP-02 no ambiente publico. |
| Backup e restauracao | Parcial tecnico | Scripts seguros criados para dump com hash, 7 diarios, 4 semanais e restauracao restrita a banco isolado. Sintaxe validada sem credenciais. | Identificar provedor/versao do MySQL e armazenamento externo, cadastrar segredos e executar a primeira copia/restauracao real controlada. Nunca sobrescrever a unica copia. |
| Compatibilidade | Aceite pelo cliente | Interfaces responsivas ja foram inspecionadas tecnicamente; o cliente assumiu os testes finais de navegadores e celulares. | Registrar os resultados informados pelo cliente no encerramento. |

## Validacao de referencia

Em 28/10/2026, na arvore local ainda sem commit ou publicacao:

- 77 testes Java aprovados, sem falhas, erros ou ignorados;
- lint e build de producao React aprovados;
- modelos de e-mail, eventos pos-commit e handoff administrativo para WhatsApp validados localmente;
- configuracao segura do perfil `prod` e runbook de backup/Flyway preparados, sem conexao ou alteracao no banco real;
- bundles antigos do Spring removidos e o build atual sincronizado em `src/main/resources/static`;
- nenhum segredo SMTP foi cadastrado e nenhum e-mail real foi enviado;
- nenhum commit, push, deploy ou homologacao publica foi executado.

Em 26/10/2026, apos a reconstrucao do servico MySQL no Aiven, foram comprovados publicamente no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`:

- HTTP 200 e SHA completo servido pelo Render;
- hashes publicos de `index-CtONrlfO.js` e `index-BmPCfaig.css` identicos aos arquivos do commit;
- 181 produtos ativos e pagamento ainda identificado como `SIMULADO`;
- `GET /api/v1/auth/csrf` com token, cabecalho `X-CSRF-TOKEN` e cookie de sessao;
- logout sem token e com token invalido rejeitados com HTTP 403;
- logout com token valido aceito com HTTP 204;
- recuperacao de senha sem token rejeitada com HTTP 403 e, com token valido para e-mail inexistente, aceita com HTTP 200;
- login da conta CLIENTE de homologacao aceito com HTTP 200 e logout autenticado aceito com HTTP 204.

Essa evidencia conclui a homologacao publica do CSRF. Nao comprova ainda os fluxos completos de ADMIN, COLABORADOR, RCP-02, pagamento real ou as regras comerciais mantidas somente na arvore local.

Em 15/10/2026 foram registrados para as regras comerciais:

- testes direcionados do agente principal: 5 aprovados, sem falhas, erros ou ignorados;
- QA independente: 5 testes de fluxo aprovados, sem falhas, erros ou ignorados;
- suite Java completa: 69 aprovados, sem falhas, erros ou ignorados;
- lint e build React aprovados; bundle ativo `index-BBO_9H0d.js` e CSS `index-BmPCfaig.css` sincronizados com hashes iguais;
- nenhuma credencial ou integracao real de pagamento/frete foi usada; as regras de `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4` foram publicadas em 26/10/2026 pelo commit `c72100a1d8ec4015c53d4bf65b4ed079781c6c54`;
- pagamento permanece `SIMULADO`.

Na tentativa de homologacao do CSRF em 15/10/2026, `HEAD` e `origin/main` estavam no SHA `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`. Duas execucoes do verificador publico terminaram em `curl (28)` sem bytes; o painel do Render exigiu login. Portanto, HTTP 200, SHA servido, bundles, hashes, catalogo e smoke CSRF publicos nao foram comprovados nesta data.

Em 14/10/2026 foram registrados para a alteracao local de CSRF:

- 2 testes direcionados de CSRF aprovados, sem falhas, erros ou testes ignorados;
- 28 testes de integracao afetados aprovados, sem falhas, erros ou testes ignorados;
- 66 testes Java aprovados;
- lint do frontend aprovado;
- build de producao do frontend aprovado;
- empacotamento Maven aprovado;
- assets ativos com hashes iguais em `frontend/dist`, `src/main/resources/static`, `target/classes/static` e no JAR;
- nenhuma publicacao no GitHub ou no Render foi autorizada ou executada para essa alteracao.

Na auditoria anterior, em 25/09/2026, foram registrados:

- 60 testes Java aprovados, sem falhas, erros ou testes ignorados;
- lint do frontend aprovado;
- build de producao do frontend aprovado;
- publicacao funcional anteriormente confirmada em 22/09/2026;
- duas tentativas de reconfirmacao do Render em 25/09/2026 encerradas por timeout durante o cold start, o que nao comprova indisponibilidade nem uma nova publicacao.

Esses resultados sao evidencias historicas de momentos diferentes. A validacao local de 14/10/2026 nao comprova publicacao, e a publicacao funcional anterior nao autoriza afirmar que o ambiente publico contem a protecao CSRF sem um novo deploy e smoke test do mesmo commit.

## Trabalho que pode continuar sem o dono

1. Reexecutar testes Java, lint, build e empacotamento.
2. Limpar bundles estaticos duplicados e conferir o pacote servido pelo Spring.
3. Repetir os fluxos autenticados de ADMIN, CLIENTE e COLABORADOR.
4. Ensaiar backup e restauracao em ambiente isolado.
5. Registrar a matriz de compatibilidade em navegadores e celular.
6. Preparar minutas de RCP-01 e RCP-03, sem apresenta-las como aprovadas.
7. Preparar adaptadores e testes simulados para pagamento, SMTP e frete, sem declarar integracao real.
8. Depois de autorizacao explicita, publicar a protecao CSRF e homologar os fluxos mutaveis no ambiente publico.

## Decisoes reservadas ao dono

1. Bases legais, prazos de retencao e responsaveis pelos tratamentos de dados.
2. Aprovacao dos textos juridicos e das politicas comerciais.
3. Gateway de pagamento, conta, credenciais e regras financeiras.
4. Criacao da conta Brevo, acesso DNS, remetente verificado e segredos SMTP.
5. Credenciais/autoridade para inventario, backup e migracao controlada do banco real.
6. Aprovacao juridica dos textos e definicoes de retencao/publicidade que permanecem em espera.

## Decisoes fechadas em 28/10/2026

- Provedor de e-mail: Brevo SMTP Relay, porta 587 com STARTTLS; segredo somente no Render.
- Entrega: realizada pela propria Vita Fortis; atendimento operacional por WhatsApp manual a partir do ADMIN.
- Frete gratis: somente quando o valor dos produtos apos descontos for estritamente superior a R$ 200,00.
- Catalogo FTW: importacao inativa; a loja informa preco/estoque e ativa os produtos gradualmente.
- Backup: copia diaria externa com rotacao, checksum e restauracao mensal em banco isolado.
- Flyway: nenhuma ativacao cega; inventario, clone restaurado e baseline/migracao reconciliadora antes do perfil `prod`.
- Compatibilidade final: testes de navegadores e celulares serao executados pelo cliente.

## Criterio para declarar o projeto concluido

Somente declarar conclusao integral quando as dependencias do dono estiverem definidas, as integracoes reais estiverem homologadas, backup/restauracao e compatibilidade estiverem comprovados e o mesmo commit tiver testes locais, publicacao e smoke test publico aprovados.
