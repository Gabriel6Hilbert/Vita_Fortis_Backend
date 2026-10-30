---
name: implement-feature
description: Implementar uma funcionalidade autorizada do Vita Fortis a partir de RF, RN, RNF ou RCP, com escopo, rastreabilidade e validação proporcional.
---

1. Identifique o requisito na `STATUS_CONSOLIDADO_V2.md` e confirme que a solicitação é implementação, não apenas diagnóstico.
2. Consulte `MAPA_ARQUIVOS.md`, informe qual arquivo específico será alterado e preserve mudanças locais existentes.
3. Registre comportamento atual, critérios de aceite, perfis afetados e decisões reservadas ao proprietário.
4. Mantenha o fluxo `BANCO -> BACKEND -> API -> FRONTEND`; autorização e regras de negócio pertencem ao backend.
5. Faça a menor mudança coesa. Não reimplemente requisito marcado como concluído sem evidência de regressão.
6. Use as skills de validação da camada afetada e `$requirements-traceability` antes da entrega.
7. Separe alterado, testado, não testado, pendente e publicação. Não faça push ou deploy sem autorização explícita.
