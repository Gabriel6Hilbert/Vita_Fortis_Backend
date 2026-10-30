# Arquitetura da equipe multiagente

## Modelo operacional

O Codex principal atua como orquestrador. Ele consulta primeiro `MAPA_ARQUIVOS.md` e, quando aplicável, `AGENTS.md` e a matriz canônica, define escopo e critérios de aceite, delega somente trabalho independente com benefício claro e consolida as evidências. A implementação continua no repositório atual; não existe serviço externo de orquestração.

A execução é proporcional à complexidade: tarefas simples não usam subagentes; tarefas médias podem usar no máximo um; tarefas complexas podem usar até dois simultâneos. A equipe completa não é convocada automaticamente, e a delegação é evitada quando seu custo de coordenação supera o benefício. Cada delegação informa objetivo distinto, arquivos permitidos, modo somente leitura ou edição, critérios de aceite e validação esperada. Agentes que possam tocar o mesmo arquivo trabalham em sequência, e cada subagente é encerrado após entregar seu resultado.

O contexto também é proporcional: o orquestrador lê somente arquivos e skills relevantes, reutiliza resumos de material já analisado e evita logs ou diffs extensos quando uma síntese preserva a evidência necessária. Quando houver seleção documentada de modelo, tarefas simples priorizam modelos econômicos; modelos mais capazes e raciocínio elevado ficam reservados a arquitetura, segurança, migrações críticas e problemas complexos. O modelo global não é alterado sem autorização, e identificadores ou parâmetros não documentados nunca são presumidos.

## Papéis

| Papel | Responsabilidade | Limites obrigatórios |
| --- | --- | --- |
| `backend-java` | Spring Boot, REST, Security, validações, pedidos, estoque, cashback, pagamentos simulados e testes Java. | Não define regra financeira/comercial e não publica. |
| `frontend-react` | React, rotas, componentes, formulários, responsividade, sessão e contratos com API. | Preserva identidade visual; não usa `localStorage` como fonte definitiva de negócio. |
| `database` | MySQL, JPA, integridade, índices, transações, Flyway e ensaios isolados de backup/restauração. | Nunca executa mudança destrutiva ou produção sem autorização. |
| `qa-tester` | Testes Java/frontend, regressão, fluxos autenticados, contratos e critérios de aceite. | Revisa de forma independente e não altera o código avaliado salvo pedido expresso. |
| `security-reviewer` | Sessão, CSRF, autenticação, autorização, isolamento por perfil, LGPD técnica e segredos. | Não decide base legal nem política jurídica. |
| `devops-release` | Build, pacote, Git, Render, smoke tests e checklist de homologação. | Não faz push, merge, deploy ou alteração externa sem autorização explícita. |

## Fluxo recomendado

1. O orquestrador consulta o mapa, classifica a complexidade e seleciona somente as skills aplicáveis.
2. A tarefa é executada diretamente ou delegada dentro dos limites econômicos, no menor conjunto de arquivos possível e sem objetivos duplicados.
3. QA e Security entram quando o risco exigir revisão independente; economia não reduz verificações obrigatórias de segurança, pagamentos, estoque ou autenticação.
4. A validação começa por testes direcionados. A suíte completa é executada quando o risco ou a definição de pronto exigir, sem repetir testes aprovados após nenhuma mudança relevante.
5. O orquestrador revisa o diff e entrega um resumo curto de arquivos alterados, testes, erros, pendências e publicação. Decisões do proprietário são registradas como dependências, nunca presumidas.
6. Em caso de limite de uso, o estado, as evidências concluídas e as pendências são registrados para retomada, evitando novos ciclos sem progresso.

## Persistência e suporte da versão atual

As skills são persistidas em `.agents/skills`, formato local de repositório documentado pela OpenAI. Os papéis ficam neste documento e são instanciados pelo recurso nativo de subagentes quando uma tarefa justificar delegação. Não foram criados arquivos TOML de agentes: a versão instalada não apresentou um esquema oficial de configuração local para papéis nomeados, e propriedades não documentadas não devem ser inventadas.
