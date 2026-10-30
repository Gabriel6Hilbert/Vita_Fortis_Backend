# Status dos requisitos Vita Fortis V2

Atualizado em: 28/09/2026.

A fonte unica e atual de acompanhamento e [STATUS_CONSOLIDADO_V2.md](STATUS_CONSOLIDADO_V2.md). Este arquivo permanece como entrada curta para quem procurar o antigo status de requisitos.

## Resumo atual

- **Concluido:** nucleo comercial e administrativo, estoque apos aprovacao, cashback, catalogo, carrinho, checkout interno, pedidos, cupons, importacao, filtros, relatorios, recuperacao segura por token e RCP-02.
- **Parcial tecnico:** envio real da recuperacao de senha, estrategia de migracao/Flyway, nova homologacao publica, backup/restauracao e matriz final de compatibilidade.
- **Depende do dono:** RCP-01, aprovacao das politicas RCP-03, pagamento real, provedor SMTP, eventual transportadora externa e preco/estoque dos produtos FTW importados.

Recuperacao de senha nao esta pendente como funcionalidade interna: token, expiracao, uso unico, endpoints e telas existem. O que falta e a entrega real por um SMTP escolhido e configurado.

LGPD tambem nao deve ser registrada genericamente como ausente: o RCP-02 esta implementado. Permanecem dependentes de definicao do dono/juridico o inventario e as bases legais do RCP-01 e a aprovacao dos textos do RCP-03.

Estoque e relatorios estao concluidos conforme a regra implementada e os testes de referencia. Qualquer mudanca na formula de cashback, na politica comercial de estoque ou no fornecedor externo deve ser tratada como nova decisao, nao como falha da implementacao atual.
