---
name: react-validation
description: Validar alterações React, TypeScript e Vite do Vita Fortis, incluindo lint, build, contratos de API e limites da inspeção responsiva.
---

1. Trabalhe em `frontend/src`; nunca edite manualmente assets com hash em `src/main/resources/static/assets`.
2. Execute `pnpm.cmd lint` e `pnpm.cmd build` dentro de `frontend`.
3. Registre códigos de saída, avisos relevantes e nomes dos artefatos gerados.
4. Confira rotas, estados de carregamento/erro/vazio e contratos em `frontend/src/types/api.ts` e `frontend/src/services/api.ts` quando afetados.
5. Build e lint não comprovam comportamento, responsividade nem ambiente público. Declare essas fronteiras e use inspeção real quando exigida.
6. Copiar o build para o Spring é uma etapa separada; não substitua o bundle servido sem escopo explícito.
