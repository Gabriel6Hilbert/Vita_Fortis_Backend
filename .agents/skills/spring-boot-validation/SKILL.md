---
name: spring-boot-validation
description: Validar mudanças Java e Spring Boot do Vita Fortis com compilação, testes direcionados e suíte completa quando proporcional ao risco.
---

1. Identifique os testes relacionados em `src/test/java` e confirme que não acessam produção.
2. Para testes múltiplos no PowerShell, mantenha o seletor entre aspas: `.\mvnw.cmd "-Dtest=TesteA,TesteB" test`.
3. Execute primeiro testes direcionados; para entrega funcional de backend, execute também `.\mvnw.cmd test` ou justifique a limitação.
4. Registre comando, código de saída, total de testes, falhas, erros e ignorados a partir do resumo Maven.
5. Não trate inicialização parcial, saída truncada ou ausência do resumo final como aprovação.
6. Se a mudança afetar pacote/release, use `$release-readiness` depois dos testes.
