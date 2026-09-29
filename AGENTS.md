# Regras de trabalho com IA - Vita Fortis

Estas regras valem para todo o repositório. O objetivo é manter cada alteração pequena, rastreável e econômica em contexto.

## Antes de editar

1. Leia `PAINEL_ACOMPANHAMENTO.md` e use `MAPA_ARQUIVOS.md` para localizar a área solicitada.
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

## Entrega obrigatória

Ao terminar, separar claramente:

- alterado;
- testado;
- não testado;
- pendente;
- arquivos modificados;
- publicação: feita ou não feita.

Atualize `PAINEL_ACOMPANHAMENTO.md` somente quando a tarefa alterar o estado real do projeto. Propostas não devem aparecer como concluídas.

