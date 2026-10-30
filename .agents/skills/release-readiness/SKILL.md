---
name: release-readiness
description: Preparar e avaliar uma release do Vita Fortis com commit alvo, testes, pacote e checklist de homologação, sem publicar automaticamente.
---

1. Registre branch, commit alvo e `git status`; preserve alterações não relacionadas.
2. Execute as validações Java e React aplicáveis e `git diff --check`.
3. Confira se `src/main/resources/static/index.html` referencia assets existentes e se o bundle corresponde à fonte validada.
4. Separe evidência local, commit/push, deploy e verificação pública. Uma etapa não comprova as demais.
5. Prepare smoke tests de health, páginas, API, perfis e RCP-02 para o mesmo commit.
6. Não faça commit, push, merge, deploy, migração ou alteração externa sem autorização explícita.
7. Render timeout/cold start não prova regressão nem sucesso; repita de modo controlado ou registre como inconclusivo.
