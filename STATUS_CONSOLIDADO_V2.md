# Vita Fortis V2 - matriz consolidada de status

Revisado em: 26/10/2026
Evidencia tecnica mais recente: homologacao publica do CSRF concluida no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`; regras comerciais validadas e versionadas localmente no commit `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4`, ainda sem push ou deploy.

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
| RF15/RF18 - retirada e revisao | Concluido localmente | Retirada sem frete, preparacao em ate 24 horas, estado `DISPONIVEL_RETIRADA` e revisao de subtotal, desconto, frete, prazo e total, versionados no commit local `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4`. | Endereco e horario de retirada dependem do gerente; push e publicacao comercial nao realizados. |
| RF16 - frete | Parcial tecnico | Backend atende MG, SP, RJ e ES; frete gratis usa comparacao estrita `> 200` sobre produtos apos desconto; fora de BH e regiao a interface informa ate 5 dias uteis. | Municipios e prazo de BH/regiao, tarifa abaixo do limite, transportadora e rastreamento dependem de definicao. A tarifa fixa anterior continua configuravel, sem nova formula. |
| RF28/RF30 - produtos, categorias e atributos | Concluido | Inativacao logica de produtos e cadastros persistentes de categorias e atributos. | Preco e estoque dos produtos FTW importados dependem do dono antes da ativacao. |
| RNF04 - cupons | Concluido | Desconto fixo ou percentual, validade, valor minimo, limite de usos e protecao concorrente. | Nenhuma pendencia funcional conhecida. |
| RF33-RF35 - importacao | Concluido | Previa CSV/XLSX, validacao integral, transacao e historico persistente com arquivo, hash, data, responsavel e resultado. | Nenhuma pendencia funcional conhecida. |
| RF36 - consulta de pedidos | Concluido | Filtros combinaveis usam dados persistidos no fluxo banco, backend, API e frontend. | Nenhuma pendencia funcional conhecida. |
| RF38-RF40 - indicadores e relatorios | Concluido | Filtros administrativos, vendas aprovadas, cupons, estoque baixo e exportacoes CSV/XLSX/PDF. | Nenhuma pendencia funcional conhecida. |
| RF07 - recuperacao de senha | Parcial tecnico | Token seguro, expiracao, uso unico, endpoints e telas estao implementados. | A equipe pode testar localmente; entrega real depende do provedor SMTP definido pelo dono. |
| RCP-02 - consentimento e direitos do titular | Concluido | Consentimento e solicitacoes de acesso, correcao e exclusao possuem isolamento por usuario, tratamento administrativo, retencao e auditoria. | Executar novamente o fluxo completo no ambiente publico na proxima homologacao. |
| Seguranca de sessao e CSRF | Concluido | A protecao vinculada a sessao foi homologada publicamente no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`: emissao de token e cookie, rejeicao sem token ou com token invalido, aceitacao com token valido, login CLIENTE, logout autenticado e recuperacao de senha foram comprovados. | Manter regressao nas proximas alteracoes de autenticacao e nas novas operacoes mutaveis. |
| RCP-01 - base legal e inventario | Depende do dono | A estrutura tecnica pode ser levantada e uma minuta pode ser preparada. | Dono/juridico deve definir finalidade, base legal, prazo de retencao e responsavel por cada tratamento. |
| RCP-03 - politicas | Depende do dono | As paginas e a estrutura de publicacao podem ser preparadas. | Aprovar privacidade, cookies, termos, entrega/retirada e troca/devolucao de modo coerente com o RCP-01. |
| RF17 - pagamento | Parcial tecnico | Novos pedidos apresentam e aceitam Pix ou cartao; a conciliacao simulada permanece restrita ao ADMIN, sem armazenar numero do cartao ou CVV. | Pagamento real permanece pendente: escolher gateway, criar conta, fornecer credenciais e definir parcelamento, juros, validade do Pix, estorno e webhook. |
| RF19 - e-mails transacionais | Depende do dono | Recuperacao de senha esta preparada para SMTP; confirmacao/extrato pode usar a mesma infraestrutura apos definicao. | Escolher provedor, remetente e aprovar os modelos de mensagem. |
| Produtos FTW importados | Depende do dono | 179 produtos foram preservados como rascunho, inativos, sem preco e com estoque zero; a ativacao invalida e bloqueada. | Informar preco, estoque e quais produtos devem ser ativados. |
| Migracoes de banco | Parcial tecnico | A migracao do RCP-02 possui teste proprio. | Flyway continua desabilitado na configuracao atual; preparar e validar a estrategia antes de qualquer aplicacao em producao. |
| Homologacao publica | Parcial tecnico | Em 26/10/2026 o commit CSRF foi comprovado publicamente com HTTP 200, SHA completo, hashes dos bundles correspondentes, 181 produtos ativos, pagamento simulado e fluxos publicos de CSRF aprovados. | Homologar ADMIN, COLABORADOR e RCP-02 no ambiente publico; regras comerciais continuam apenas locais. |
| Backup e restauracao | Parcial tecnico | O procedimento pode ser ensaiado em banco isolado sem intervencao do dono. | Executar restore controlado, comparar dados e guardar evidencias; producao exige acesso autorizado. |
| Compatibilidade | Parcial tecnico | Interfaces responsivas ja foram inspecionadas em fluxos anteriores. | Registrar uma matriz final de navegadores, desktop e celular no commit de encerramento. |

## Validacao de referencia

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
- nenhuma credencial ou integracao real de pagamento/frete foi usada; as regras foram versionadas localmente em `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4`, sem push ou deploy;
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
4. Provedor SMTP, remetente e modelos de e-mail.
5. Uso ou nao de transportadora externa.
6. Precos, estoques e ativacao dos produtos importados.

## Criterio para declarar o projeto concluido

Somente declarar conclusao integral quando as dependencias do dono estiverem definidas, as integracoes reais estiverem homologadas, backup/restauracao e compatibilidade estiverem comprovados e o mesmo commit tiver testes locais, publicacao e smoke test publico aprovados.
