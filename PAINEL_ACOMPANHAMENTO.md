# Painel de acompanhamento - Vita Fortis

Este é o ponto de entrada para acompanhar o projeto no iPhone, no PC e em novos chats com IA.

## Acesso rápido

- [Mapa dos arquivos](MAPA_ARQUIVOS.md)
- [Modelo de pedido para IA](MODELO_PEDIDO_IA.md)
- [Status completo dos requisitos](STATUS_CONSOLIDADO_V2.md)
- [Como executar o projeto](README.md)

## Legenda

- ⬜ A fazer
- 🟡 Em andamento
- 🔵 Implementado, aguardando teste
- 🟣 Testado localmente
- 🟢 Publicado e verificado
- 🔴 Bloqueado por decisão, acesso ou credencial

## Etapa atual

### Organização do trabalho com IA

Estado: 🟢 Publicado e verificado

- [x] Criar regras automáticas de escopo no `AGENTS.md`.
- [x] Criar mapa inicial de arquivos.
- [x] Criar modelo de solicitação pequena e rastreável.
- [x] Criar painel legível no celular e no computador.
- [x] Revisar e autorizar a organização com o proprietário.
- [x] Publicar no GitHub após aprovação explícita.
- [ ] Confirmar a abertura dos links no iPhone e no PC.

## Próximas etapas

### Segurança de tráfego

Estado: ⬜ A fazer

- [ ] Confirmar domínio, DNS e proxy utilizados hoje.
- [ ] Definir países permitidos e exceções necessárias.
- [ ] Proteger login, recuperação de senha e APIs contra abuso.
- [ ] Evitar acesso que contorne a proteção pelo endereço do Render.
- [ ] Testar bloqueio, rate limit e tráfego brasileiro legítimo.

### Organização do repositório

Estado: ⬜ A fazer

- [ ] Classificar alterações locais existentes.
- [ ] Identificar bundles ativos e obsoletos.
- [ ] Remover somente arquivos gerados comprovadamente sem uso.
- [ ] Consolidar os documentos de status sem apagar histórico necessário.

### Encerramento técnico

Estado: ⬜ A fazer

- [ ] Validar Flyway em banco isolado.
- [ ] Ensaiar backup e restauração.
- [ ] Executar testes Java, lint e build do frontend.
- [ ] Homologar ADMIN, CLIENTE, COLABORADOR e RCP-02.
- [ ] Registrar compatibilidade em iPhone, desktop e navegadores.
- [ ] Publicar e verificar o mesmo commit homologado.

### Dependências do proprietário

Estado: 🔴 Aguardando decisões ou insumos

- RCP-01 e aprovação jurídica do RCP-03.
- Gateway e regras de pagamento real.
- Provedor SMTP, remetente e modelos de e-mail.
- Decisão sobre transportadora externa.
- Preço, estoque e ativação dos produtos FTW importados.

## Registro da tarefa atual

Objetivo: organizar o acompanhamento e limitar o escopo das alterações feitas com IA.

Arquivos desta tarefa:

- `AGENTS.md`
- `PAINEL_ACOMPANHAMENTO.md`
- `MAPA_ARQUIVOS.md`
- `MODELO_PEDIDO_IA.md`

Fora do escopo desta tarefa:

- código Java;
- código React;
- banco de dados;
- configuração de produção;
- publicação no GitHub ou Render.

Última atualização deste painel: 29/09/2026.
