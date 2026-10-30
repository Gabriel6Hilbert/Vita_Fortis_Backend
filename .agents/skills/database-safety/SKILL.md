---
name: database-safety
description: Avaliar com segurança esquema, JPA, MySQL, Flyway, integridade, backup e restauração do Vita Fortis antes de qualquer mudança de banco.
---

1. Identifique ambiente, URL de banco, perfil Spring e alvo exato sem exibir segredos.
2. Compare entidades, `ddl-auto`, migrações existentes e configuração Flyway por perfil.
3. Para nova migração, prefira alteração aditiva, nome versionado em minúsculas compatível com H2/MySQL/Linux e teste direcionado.
4. Valide integridade, índices, chaves, transações e concorrência onde a regra de negócio depender delas.
5. Backup/restauração deve usar banco isolado e dados sanitizados, com alvo conferido antes de qualquer escrita.
6. Nunca execute DDL destrutivo, baseline, migrate ou restore em produção sem autorização explícita e plano de recuperação.
7. Entregue diagnóstico, ensaio executado, riscos, rollback e decisão pendente; não confunda plano com migração aplicada.
