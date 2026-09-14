# Revisão de homologação - 14/09/2026

Escopo: estabilizar os fluxos existentes, mantendo pagamento simulado e as regras atuais de reserva de estoque e extrato do colaborador. Não representa entrega integral dos requisitos V2.

## Correções

- Revisão do pedido calculada no backend, sem reservar estoque: preço, disponibilidade, cupom, frete, prazo e total. A confirmação rejeita total diferente do revisado.
- Checkout apresenta frete e total antes de confirmar; preserva endereço salvo, permite voltar para editar e informa erros de atualização da sacola.
- Falha na limpeza da sacola após criação não faz a tela tratar o pedido como não criado.
- Consulta de pedido carrega itens e histórico separadamente: evita itens duplicados e devolução excessiva de estoque no cancelamento.
- Resumo de cashback deixa de devolver detalhes dos pedidos de outros compradores. Extrato próprio permanece disponível.
- Importação valida cabeçalho, SKU repetido, campos, preço, estoque e situação antes de gravar qualquer produto; leitura CSV preserva aspas, separadores e quebras de linha.
- XLSX preserva campos do CSV; PDF distribui registros por várias páginas e produtos sem preço não interrompem o relatório.
- Métricas de faturamento consideram pagamentos aprovados.
- Endpoint público `/api/v1/loja/versao` informa o commit do Render e pagamento simulado. `node scripts/verificar-release.mjs <commit>` verifica release, catálogo e hashes dos arquivos públicos.

## Validação

- 30 testes Java aprovados, incluindo revisão sem baixa, total alterado com rollback, aprovação repetida bloqueada, cancelamento com restituição exata, importação sem gravação parcial e relatório com 120 produtos.
- Build React e pacote Maven aprovados.
- Navegador local: login real, produto na sacola, revisão com entrega de R$ 149,90 + R$ 15,00 = R$ 164,90, prazo de 5 dias, criação do pedido e sacola limpa. Checkout inspecionado em desktop e largura de 390 px.
- Testes locais usam banco H2 isolado. Não foram criados pedidos comerciais no banco do Render.

## Publicação

Antes desta revisão, a página pública servia os bundles do commit `33cee94`, apesar de `e34398b` já estar no GitHub. O catálogo público retornou HTTP 200 com 181 produtos ativos.

A publicação desta revisão só deve ser considerada concluída após o Render indicar deploy bem-sucedido e o script confirmar commit e hashes públicos.

## Fora desta revisão

PagBank real, SMTP, transporte real, políticas completas, histórico persistente de importação, gestão dinâmica de categorias e adequação integral ao RF25 continuam como etapas posteriores. O estoque continua reservado ao criar o pedido e devolvido no cancelamento; a fórmula atual de cashback foi mantida.
