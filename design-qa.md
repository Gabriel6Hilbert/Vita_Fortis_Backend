# Design QA do mosaico de objetivos

## Evidências

- Fonte visual: `C:\Users\USURIO~1\AppData\Local\Temp\codex-clipboard-cfcaca93-a921-4859-abfd-6c88db2db1ec.png`
- Referência do estado anterior: `C:\Users\USURIO~1\AppData\Local\Temp\codex-clipboard-06c90e96-0e4e-4abb-bfce-fe9b477c08f8.png`
- Implementação: `http://127.0.0.1:4173/`, captura realizada no navegador interno do Codex nesta sessão
- Viewport observado: 687 x 824 CSS px, densidade padrão do navegador
- Fonte: fotografia de uma tela de notebook, 900 x 1600 px; usada como referência de composição, não como alvo de correspondência pixel a pixel
- Estado: página inicial, seção Eu quero, sem autenticação

## Comparação completa

A referência pede quatro peças fotográficas organizadas em duas linhas, com tipografia forte sobre as imagens e chamadas para os produtos. A implementação reproduz essa hierarquia com quatro fotografias próprias, composição 2 x 2, recortes alternados, títulos em caixa alta e botões visíveis. Em telas largas, os cards usam proporções alternadas 7/5 e 5/7; no viewport intermediário observado, a grade permanece em duas colunas equilibradas. A ordem também segue a referência: massa e emagrecimento na primeira linha; saúde e performance na segunda.

## Comparação focada

O recorte da seção foi verificado no navegador. Os quatro assuntos permanecem reconhecíveis, a sobreposição escura mantém contraste, números e chamadas usam o amarelo da marca, e os textos não apresentam corte ou sobreposição. Não foi necessária outra região focada porque a solicitação se limita ao mosaico de objetivos.

## Superfícies de fidelidade

- Tipografia: hierarquia forte, caixa alta, pesos e quebras adequados; sem truncamento.
- Espaçamento e ritmo: duas linhas, separação mínima de 4 px, arestas retas e recortes contínuos; sem overflow visível.
- Cores: preto, branco e amarelo da identidade atual; contraste suficiente sobre as imagens.
- Imagens: quatro arquivos WebP nítidos, coerentes entre si e otimizados; nenhum placeholder ou desenho em CSS.
- Conteúdo: os quatro objetivos e seus filtros foram preservados; o primeiro link foi testado e abriu o catálogo com `objetivos=EMAGRECIMENTO`.

## Histórico de iteração

- Primeira captura: o formato ainda tinha espaçamento amplo, cantos arredondados e ordem diferente da referência.
- Ajuste realizado: mosaico contínuo com arestas retas, proporções 7/5 e 5/7 e ordem visual equivalente à referência.
- Captura posterior: recortes, textos e botões permaneceram legíveis, sem sobreposição ou overflow.

## Findings

Nenhuma diferença P0, P1 ou P2 permanece. A referência contém textos diferentes e uma linguagem visual vermelha; a adaptação para os quatro objetivos reais e para o amarelo da Vita Fortis é intencional.

## Follow-up Polish

- P3: conferir com o cliente se prefere manter duas colunas em celulares grandes ou forçar uma coluna abaixo de 600 px, como implementado para celulares menores.

## Verificações

- Link principal dos objetivos: aprovado.
- `npm run lint`: aprovado.
- `npm run build`: aprovado.
- Console do navegador: a superfície atual do navegador interno não expôs exportação local dos logs; nenhuma falha visual ou de navegação foi observada.

final result: passed

---

# Design QA das seções comerciais da home

## Evidências

- Benefícios: referência `codex-clipboard-a7a2f51b-4e24-42ac-8f26-dbbc3f9b8050.png`.
- Mais vendidos: referências `codex-clipboard-e6d2ba9d-1a0a-487b-a80e-6f58fe7f418c.png` e `codex-clipboard-fb80dc0c-db81-420d-9750-676f86b922e9.png`.
- Uma Linha, Uma Função: referências `codex-clipboard-bffbff84-5bd7-4a3e-9e44-1bf4e80ed4a2.png` e `codex-clipboard-423379fe-99f8-4b38-aead-c2256ff275ac.png`.
- Implementação observada em `http://127.0.0.1:4173/` nos viewports 1440 x 900 e 390 x 844 CSS px.

## Resultado

- O benefício “Pedido simplificado” foi removido; os três restantes ocupam uma única linha no desktop e uma grade compacta no celular.
- “Escolhas para cada rotina” foi substituído por “MAIS VENDIDOS”, usando três `ProductCard` e ranking real da API por `totalVendido`.
- “Qual é o seu esporte?” foi substituído por “Uma Linha, Uma Função”, com cinco cards fotográficos em uma linha no desktop e faixa horizontal rolável no celular.
- Os cinco links representam filtros e rotas existentes, com imagens reais e sem placeholders visuais.

## Verificações

- Desktop: cinco colunas confirmadas na faixa de funções.
- Celular: três benefícios confirmados e faixa de funções com rolagem horizontal.
- `npm run lint`: aprovado.
- `npm run build`: aprovado.
- A prévia local não estava conectada ao backend, portanto os cards dinâmicos exibiram o estado de erro esperado; a integração foi validada por build e pelo contrato `maisVendidos=true` já suportado no backend.

final result: passed
