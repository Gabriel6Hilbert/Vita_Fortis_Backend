# Regras de trabalho com IA - Vita Fortis

Estas regras valem para todo o repositório. O objetivo é manter cada alteração pequena, rastreável e econômica em contexto.

## Contexto do projeto

- E-commerce monolítico: Spring Boot 3.5.3 e Java 17 no backend; React, TypeScript e Vite no frontend; MySQL em execução normal e H2 nos testes.
- O React é compilado e servido pelo Spring em `src/main/resources/static`; a fonte fica em `frontend/src`.
- Documento funcional canônico: `STATUS_CONSOLIDADO_V2.md`. Relatórios anteriores são evidência histórica, não substitutos da matriz.
- Arquitetura e papéis multiagente: `docs/agent-architecture.md`. Backlog técnico: `docs/technical-backlog.md`.

## Antes de editar

1. Consulte `PAINEL_ACOMPANHAMENTO.md` quando a tarefa envolver estado do projeto e use `MAPA_ARQUIVOS.md` para localizar a área solicitada.
2. Verifique `git status --short` e preserve alterações que já existiam.
3. Informe ao usuário, antes da edição:
   - objetivo entendido;
   - arquivos que pretende abrir;
   - arquivos que pretende alterar;
   - testes que pretende executar.
4. Comece somente pelos arquivos indicados no mapa. Amplie a leitura apenas quando uma dependência real exigir isso e explique a ampliação.
5. Não faça varredura ampla do projeto quando o pedido indicar uma tela, endpoint, serviço ou requisito específico.

## Durante a alteração

- Faça a menor mudança coesa capaz de atender ao pedido.
- Não misture limpeza, refatoração ou melhoria visual não solicitada.
- Não edite manualmente arquivos gerados em `src/main/resources/static/assets`.
- Altere o código-fonte React em `frontend/src`; gere o bundle somente depois da validação.
- Para dados comerciais, use o fluxo `BANCO -> BACKEND -> API -> FRONTEND`; não use mocks ou `localStorage` como fonte definitiva.
- Não confie em IDs, preços, permissões, descontos ou fretes fornecidos pelo frontend.
- Não invente política jurídica, percentual, preço, estoque, credencial ou decisão comercial ausente.
- Não publique, faça push, migre banco de produção ou altere serviço externo sem pedido explícito.

## Validação proporcional

- Somente documentação: conferir links, ortografia, `git diff --check` e o diff final.
- Somente frontend: `pnpm.cmd lint` e `pnpm.cmd build` em `frontend`.
- Backend: testes direcionados da área; antes de entrega funcional, executar também `mvnw.cmd test`.
- Banco/migração: teste direcionado em banco isolado antes de considerar produção.
- Release: testes locais, build, commit identificado, publicação e smoke test desse mesmo commit.

## Skills do repositório

- `$implement-feature`: mudança funcional autorizada a partir de RF, RN, RNF ou RCP.
- `$spring-boot-validation`: compilação e testes Java/Spring.
- `$react-validation`: lint, build e validação do frontend.
- `$ecommerce-security-review`: revisão de sessão, CSRF, autorização, perfis e exposição de dados.
- `$database-safety`: esquema, Flyway, integridade, backup ou restauração.
- `$release-readiness`: preparação de release sem publicar automaticamente.
- `$requirements-traceability`: confronto entre requisitos, código, testes e matriz canônica.

As skills ficam em `.agents/skills`. Carregue apenas as aplicáveis à tarefa.

## Política de execução econômica

### Delegação sob demanda

- Classifique a tarefa antes de delegar: tarefas simples são executadas diretamente; tarefas médias podem usar no máximo um subagente quando houver benefício claro; tarefas complexas podem usar até dois subagentes simultâneos.
- Não convoque toda a equipe automaticamente. Delegue somente trabalho independente e específico quando o ganho esperado superar o custo de coordenação.
- Não inicie subagentes com objetivos equivalentes. Encerre cada subagente após a entrega do resultado e não mantenha papéis ociosos ativos.

### Contexto mínimo

- Consulte `MAPA_ARQUIVOS.md` antes de explorar o projeto e abra somente os arquivos relevantes indicados para a tarefa.
- Não releia documentos completos já analisados sem necessidade; retome por trechos, referências ou resumos quando forem suficientes.
- Não carregue todas as skills em cada solicitação. Use apenas as exigidas pelo escopo e pelo risco.
- Prefira resumos objetivos a logs, respostas e diffs extensos; amplie detalhes somente quando forem necessários para diagnóstico, auditoria ou decisão.

### Seleção de modelo e raciocínio

- Quando a sessão oferecer seleção documentada, priorize modelos econômicos disponíveis no Codex para tarefas simples.
- Reserve modelos mais capazes e raciocínio elevado para arquitetura, segurança, migrações críticas e problemas realmente complexos.
- Não invente identificadores de modelos nem parâmetros de configuração. Não altere o modelo global sem autorização explícita do usuário.

### Validação eficiente

- Durante a implementação, execute primeiro os testes direcionados à área alterada. Execute a suíte completa quando o risco, a abrangência da mudança ou a definição de pronto exigirem.
- Não repita testes aprovados se não houve alteração relevante no código, configuração ou ambiente coberto por eles.
- Economia de tokens ou tempo não autoriza reduzir verificações obrigatórias de segurança, pagamentos, estoque ou autenticação.

### Controle e retomada

- Evite ciclos repetitivos de análise sem progresso; após uma tentativa inconclusiva, registre a evidência nova e mude a abordagem ou declare a pendência.
- Se ocorrer limite de uso, registre de forma concisa o estado da tarefa, arquivos alterados, validações concluídas e pendências necessárias para retomada.
- Nas respostas, informe somente arquivos alterados, testes, erros relevantes, pendências e estado de publicação. Não repita o histórico completo do projeto.

## Orquestração e papéis

- O agente principal mantém o escopo, divide apenas tarefas independentes e consolida resultados.
- Respeite os limites da política de execução econômica: nenhum subagente para tarefas simples, no máximo um para tarefas médias e até dois simultâneos para tarefas complexas. Não permita edição concorrente do mesmo arquivo.
- Papéis disponíveis: `backend-java`, `frontend-react`, `database`, `qa-tester`, `security-reviewer` e `devops-release`, com limites definidos em `docs/agent-architecture.md`.
- `qa-tester` e `security-reviewer` revisam de modo independente; não devem declarar aprovação sem evidência executada.
- Subagentes são papéis de execução nativos da sessão, não serviços externos persistentes. Registre quais papéis foram realmente executados.

## Aprovação humana

- Exigem autorização explícita: push, merge, release, deploy, produção, migração destrutiva, alteração de DNS/serviço externo e uso de credenciais reais.
- Permanecem decisões do proprietário: políticas jurídicas, gateway e regras financeiras, SMTP/remetente, transportadora, preços, estoques e ativação do catálogo FTW.
- Propostas, mocks e testes simulados devem permanecer identificados como tais.

## Definição de pronto

- Funcionalidade: requisito e critérios de aceite rastreados, autorização/backend preservados, testes proporcionais executados e documentação de estado atualizada somente quando a evidência mudou.
- Release: árvore e commit alvo identificados, testes e build limpos, pacote conferido, publicação autorizada e smoke test público do mesmo commit. Preparação local não equivale a publicação.

## Entrega obrigatória

Ao terminar, separar claramente:

- alterado;
- testado;
- não testado;
- pendente;
- arquivos modificados;
- publicação: feita ou não feita.

Atualize `PAINEL_ACOMPANHAMENTO.md` somente quando a tarefa alterar o estado real do projeto. Propostas não devem aparecer como concluídas.
