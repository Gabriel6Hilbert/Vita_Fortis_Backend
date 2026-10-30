# Vita Fortis - acompanhamento da entrega

Atualizado em: 28/09/2026.

Consulte a matriz canonica em [STATUS_CONSOLIDADO_V2.md](STATUS_CONSOLIDADO_V2.md). Este arquivo resume a entrega do site sem repetir uma lista paralela de pendencias.

## Concluido

- Areas autenticadas de ADMIN, CLIENTE e COLABORADOR.
- Catalogo, favoritos, avaliacoes, sacola, endereco, cupom, retirada/entrega, checkout interno e historico de pedidos.
- Painel administrativo de produtos, estoque, pedidos, usuarios, colaboradores, cupons, cashback, importacao, indicadores e relatorios.
- Baixa definitiva de estoque na aprovacao e recomposicao aplicavel no cancelamento.
- Recuperacao de senha com token seguro, expiracao, uso unico, endpoints e telas.
- RCP-02: consentimento e solicitacoes de acesso, correcao e exclusao, com isolamento, tratamento administrativo, retencao e auditoria.
- Relatorios RF38-RF40 e consulta administrativa RF36 conectados a dados persistidos.
- Identidade visual responsiva e fluxos principais inspecionados em desktop e celular nas homologacoes registradas.

## Parcial tecnico

- Repetir a homologacao publica dos tres perfis e do RCP-02 no mesmo commit da proxima entrega.
- Configurar e comprovar SMTP real depois que o provedor for escolhido.
- Validar a estrategia de Flyway antes de aplicar migracoes automaticamente em producao.
- Ensaiar backup/restauracao em ambiente isolado e registrar a matriz final de compatibilidade.
- Revisar os arquivos estaticos nao rastreados antes da proxima release.

## Depende do dono

- Definicao juridica do RCP-01 e aprovacao das politicas do RCP-03.
- Gateway de pagamento, conta, credenciais e regras financeiras.
- Provedor SMTP, remetente e modelos de e-mail.
- Decisao sobre transportadora externa.
- Preco, estoque e autorizacao de ativacao dos 179 produtos FTW importados como rascunho.

## Regra de comunicacao

Nao tratar uma integracao simulada, uma minuta ou uma validacao local como prova de operacao real. A conclusao integral exige o mesmo commit aprovado nos testes locais, publicado e confirmado por smoke test publico.
