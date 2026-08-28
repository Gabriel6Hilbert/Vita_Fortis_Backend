# Status dos requisitos Vitta Fortis 2.1

Fonte analisada: `Levantamento_Requisitos_VittaFortis_Atualizado.docx`.

## Implementado nesta revisao

- Codigo-fonte React incorporado ao repositorio em `frontend`.
- Checkout com escolha entre retirada e entrega.
- Enderecos de cliente persistentes, com endereco principal e autorizacao por proprietario.
- Frete calculado no backend; retirada sempre com frete zero.
- Revisao financeira do pedido com subtotal, desconto, frete e total em `BigDecimal`.
- Porta de pagamento desacoplada e implementacao local para desenvolvimento, sem armazenar cartao.
- Status e referencia de pagamento registrados no pedido.
- Perfil autenticado, preferencia de comunicacao e alteracao segura de senha.
- Cupom vinculado a colaborador e percentual de cashback configuravel.
- Credito, estorno, ajuste e baixa de cashback com saldo anterior, saldo novo, responsavel e justificativa.
- Area propria do colaborador no frontend, com saldo, extrato auditavel, cupons vinculados, vendas, pedidos e desempenho.
- Painel administrativo separado da conta do cliente, com produtos ativos e inativos, estoque, pedidos, usuarios, colaboradores e cupons.
- Cadastro administrativo de colaborador, ativacao/desativacao e permissao individual para relatorios.
- Formulario de cupom com vinculo ao colaborador e percentual de cashback.
- Catalogo, detalhes do produto, favoritos persistidos, avaliacoes, relacionados, sacola e checkout integrados.
- Cupom aplicado na sacola transportado ao pedido, com desconto e cashback do colaborador apresentados separadamente.
- API administrativa de produtos lista todo o inventario, inclusive itens inativos; a API publica continua ocultando inativos.
- Indicadores do painel conectados a API existente `/api/v1/admin/metricas`, usando por padrao o mes corrente.
- Relatorios CSV de pedidos, produtos, clientes, cupons e cashback.
- Remocao das APIs, rotas e textos de fidelidade por pontos e Vitta Diamante.
- Prompt reutilizavel de Clean Code em `CLEAN_CODE_PROMPT.md`.

## Dependencias externas ainda nao concluidas

Estes itens dependem de decisoes ou credenciais que o proprio documento lista como pontos para validacao:

- PagBank real e webhook: falta definir conta, credenciais, meios habilitados e politica de idempotencia.
- Frete real: falta definir regioes, transportadora e regra de calculo. Hoje existe cotacao fixa configuravel.
- E-mail transacional: falta escolher provedor, remetente e templates.
- Reserva versus baixa de estoque: falta confirmacao comercial do momento exato. Hoje o estoque e reservado ao criar o pedido e devolvido no cancelamento.

## Proximas lacunas internas

- Importacao administrativa de produtos/estoque por CSV ou XLSX com previa e historico.
- Livro de movimentacoes de estoque com motivo e responsavel para todo ajuste.
- Recuperacao de senha por e-mail com token temporario.
- Politicas completas de LGPD, cookies e solicitacoes de titular.
- Migracoes versionadas de banco para producao; o ambiente atual ainda usa `ddl-auto=update` em desenvolvimento.
- Ampliar a cobertura alem dos testes atuais de contexto, entidade e autorizacao da area do colaborador, especialmente checkout, estorno e relatorios.

Nenhum item desta ultima lista deve ser apresentado como concluido antes de sua implementacao e validacao.
