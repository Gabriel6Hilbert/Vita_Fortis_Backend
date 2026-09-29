# Modelo de pedido para IA

Copie o bloco abaixo para iniciar uma alteração pequena e controlada no Vita Fortis.

```text
Objetivo:
[descreva uma única alteração]

Onde aparece:
[página, botão, fluxo, endpoint ou requisito]

Comportamento atual:
[o que acontece hoje]

Comportamento esperado:
[o resultado desejado]

Escopo permitido:
[arquivos ou área indicada no MAPA_ARQUIVOS.md]

Fora do escopo:
[áreas que não podem ser alteradas]

Antes de editar:
1. Consulte AGENTS.md, PAINEL_ACOMPANHAMENTO.md e MAPA_ARQUIVOS.md.
2. Informe quais arquivos precisa abrir e quais pretende alterar.
3. Se precisar sair do escopo, explique o motivo antes.

Validação esperada:
[teste direcionado, lint, build, inspeção visual etc.]

Publicação:
NÃO. Apenas deixe pronto e mostre o diff.
```

## Exemplo curto

```text
Objetivo:
Melhorar a mensagem de confirmação ao abrir uma solicitação de privacidade.

Onde aparece:
Página Minha conta, seção Privacidade e dados pessoais.

Escopo permitido:
frontend/src/pages/AccountPage.tsx

Fora do escopo:
Backend, banco, painel administrativo e estilos globais.

Antes de editar, informe se o arquivo indicado é suficiente.
Execute lint e build. Não publique.
```

## Pedido ainda mais rápido

Quando o arquivo já for conhecido:

```text
Altere somente `frontend/src/pages/AccountPage.tsx` para [objetivo].
Antes de editar, confirme se existe dependência indispensável fora dele.
Não refatore outras áreas, não gere release e não publique.
Mostre no final o arquivo alterado e o que foi validado.
```
