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

Estado: 🟡 Em andamento

- [ ] Confirmar domínio, DNS e proxy utilizados hoje.
- [ ] Definir países permitidos e exceções necessárias.
- [x] Implementar proteção local contra excesso de tentativas em login, cadastro e recuperação de senha.
- [x] Reforçar validação, expiração, uso único e concorrência dos tokens de recuperação.
- [x] Implementar proteção CSRF vinculada à sessão para operações mutáveis no backend e no frontend.
- [x] Validar rejeição sem CSRF, aceitação com token válido, login, logout e recuperação de senha.
- [x] Validar localmente a proteção com testes Java, lint e build do frontend.
- [x] Publicar a proteção anterior de autenticação no GitHub.
- [x] Enviar a proteção CSRF ao GitHub no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`.
- [x] Homologar a proteção no ambiente público no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9` (26/10/2026).
- [ ] Adicionar proteção complementar na borda para login, recuperação de senha e APIs sensíveis.
- [ ] Evitar acesso que contorne a proteção pelo endereço do Render.
- [ ] Testar bloqueio, rate limit e tráfego brasileiro legítimo.

### Organização do repositório

Estado: 🟡 Em andamento

- [ ] Classificar alterações locais existentes.
- [ ] Identificar bundles ativos e obsoletos.
- [ ] Remover somente arquivos gerados comprovadamente sem uso.
- [x] Sincronizar o bundle React com proteção CSRF no pacote servido pelo Spring (TEC-03, validado localmente em 09/10/2026).
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

- RCP-01 e RCP-03 possuem minutas técnicas locais; ainda faltam retenção, decisão sobre tecnologias de publicidade, prazo de BH/região, revisão e aprovação jurídica.
- Gateway e regras de pagamento real.
- Provedor SMTP, remetente e modelos de e-mail.
- Decisão sobre transportadora externa.
- Preço, estoque e ativação dos produtos FTW importados.

## Registro da tarefa atual

Objetivo: concluir a homologação pública do CSRF e implementar localmente as regras comerciais confirmadas.

Arquivos desta tarefa:

- `src/main/java/VitaFortis/demo/config/SecurityConfig.java`
- `src/main/java/VitaFortis/demo/config/CsrfController.java`
- `frontend/src/services/api.ts`
- testes de integração e segurança em `src/test/java`
- `PAINEL_ACOMPANHAMENTO.md`

Estado comprovado até 26/10/2026:

- CSRF enviado ao GitHub no SHA completo `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`;
- homologação pública do CSRF concluída em 26/10/2026: HTTP 200, SHA completo, bundles do commit com hashes correspondentes, token e cookie de sessão, rejeição sem token, aceitação com token válido, login CLIENTE, logout autenticado e recuperação de senha comprovados;
- regras comerciais do commit funcional `b6ccf4b0198126146deb1c8c32d4fd35b1acedd4` publicadas pelo commit `c72100a1d8ec4015c53d4bf65b4ed079781c6c54`: UFs MG/SP/RJ/ES, retirada sem frete, preparação em até 24 horas, entrega em até 5 dias úteis fora de BH e região, frete grátis somente acima de R$ 200,00, Pix/cartão e conciliação por ADMIN;
- prazo de BH e região, tarifa abaixo do limite, endereço/horário de retirada, transportadora e integrações reais continuam pendentes;
- pagamento permanece simulado; o checkout comercial publicado ainda precisa de smoke test funcional completo.

Última atualização deste painel: 26/10/2026. O CSRF está homologado publicamente no commit `8c47bb281293a5d9bf586910dd6ba5084fc40ad9`. As regras comerciais foram validadas com 69 testes Java, lint e build React, publicadas pelo commit `c72100a1d8ec4015c53d4bf65b4ed079781c6c54` e verificadas com HTTP 200, SHA e hashes correspondentes. O pagamento continua simulado e o smoke test funcional completo do checkout permanece pendente.
