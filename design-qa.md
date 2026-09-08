# Design QA — sacola e checkout

- Source visual truth: `C:\Users\USURIO~1\AppData\Local\Temp\codex-clipboard-62b93c38-5204-4084-8d29-730cb89a1699.png`, `codex-clipboard-c3eafe1d-239c-4bff-a90d-151677d47fd9.png` e `codex-clipboard-40f2753a-4184-4afb-85dd-2350d30447ef.png`.
- Implementation: `frontend/src/pages/CartPage.tsx` e `frontend/src/styles/global.css`.
- Intended viewport: desktop e celular responsivo.
- State: sacola com um produto e entrega alternando entre retirada e endereço.
- Source pixels: capturas fornecidas em 1743x783, 822x657 e 1743x779.
- Implementation pixels/CSS density: indisponíveis nesta execução.

## Full-view comparison evidence

As capturas de origem mostram quatro problemas P1/P2: ausência da imagem do produto, resumo visualmente fraco, formulário de endereço ultrapassando o card e linha permanecendo após limpar a sacola. A implementação corrige esses pontos no código e o comportamento de limpeza passou em teste HTTP e teste de integração.

## Focused region comparison evidence

Não foi possível capturar a versão implementada autenticada. O frontend iniciou, mas o backend foi interrompido por uma falha ambiental do Java/Tomcat ao criar sua conexão interna de loopback.

## Comparison history

- P1 endereço excedendo o card: formulário recebeu grade específica dentro do resumo e breakpoint de uma coluna no celular.
- P1 item permanecendo após limpar: coleção gerenciada pelo JPA agora é limpa, removendo órfãos e devolvendo lista vazia.
- P2 imagem ausente: DTO do carrinho agora entrega `produtoImagemUrl`; frontend usa a imagem real ou a imagem reserva do catálogo.
- P2 resumo sem hierarquia: campos foram agrupados em seções, controles receberam largura integral e o total ganhou maior destaque.

## Findings

- P1: falta captura pós-implementação nos mesmos estados e viewports.
- P2: falta testar visualmente os seletores, o formulário completo de endereço e a confirmação de limpeza no navegador.

## Implementation checklist

- [x] Corrigir remoção total e individual.
- [x] Incluir imagem no contrato do carrinho.
- [x] Reestruturar card de produto e resumo.
- [x] Corrigir grade do endereço.
- [x] Validar build React e teste de integração.
- [ ] Capturar desktop e celular com backend ativo.
- [ ] Comparar novamente e resolver diferenças P1/P2 remanescentes.

final result: blocked
