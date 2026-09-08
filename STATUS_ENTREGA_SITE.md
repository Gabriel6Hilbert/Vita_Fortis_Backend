# Vita Fortis — acompanhamento da entrega

Atualizado em: 07/09/2026

## Concluído

- [x] Diagnóstico visual do painel administrativo e da página de ofertas.
- [x] Correção da inclusão inicial de produtos na sacola.
- [x] Correção do perfil retornado no login (`tipoUsuario`).
- [x] Sessão autenticada entre React e Spring.
- [x] Opção Home na navegação principal.
- [x] Contrato de cupons com quantidade de usos e pedidos relacionados.
- [x] Contrato de métricas com entregas e retiradas aguardando atendimento.
- [x] Build React e suíte Java validados após a primeira etapa.
- [x] Bloco 7 — inspeção responsiva de administração, sacola e checkout; validação autenticada de ADMIN, CLIENTE e COLABORADOR.
- [x] Bloco 8 — 179 produtos FTW importados com descrição, 170 imagens locais e 9 imagens reserva; todos inativos, sem preço e com estoque zero.
- [x] Recuperação de senha com token seguro, expiração, uso único, telas e integração SMTP configurável.
- [x] Inicialização do Tomcat no Windows corrigida com o conector NIO2.

## Em andamento

- [ ] Configurar as credenciais do provedor SMTP para habilitar a entrega real dos e-mails de recuperação.
- [ ] Informar preço e estoque dos produtos importados antes de ativá-los para a loja.

## Falta fazer

- [x] Bloco 2 — ofertas e novidades de ponta a ponta.
- [ ] Quadro visual no FigJam com telas, observações e situação de cada bloco.

## Implementação concluída — aguardando somente inspeção visual final

- [x] Identidade visual — preto/grafite, branco quente e amarelo dourado discreto, com a nova logo aplicada no cabeçalho e rodapé.
- [x] Bloco 1 — sidebar, datas e cards do administrador.
- [x] Bloco 3 — cadastro, edição, status, imagem, desconto, novidade, estoque e histórico de movimentações.
- [x] Bloco 4 — dashboard, pedidos, cupons, avaliações, usuários, relatórios e cashback administrativo.
- [x] Bloco 5 — catálogo, favoritos, detalhes do produto, avaliações, sacola, endereço, cupom, retirada/entrega, pagamento e histórico de pedidos.
- [x] Bloco 6 — perfil editável, endereços, alteração autenticada de senha e área exclusiva do colaborador.

## Entregue nos blocos atuais

- Paleta principal migrada do verde para preto/grafite e amarelo dourado, preservando vermelho e verde somente no símbolo original da marca.
- Botões, navegação, cards, banners, rodapé, painel administrativo e estados de foco alinhados à nova identidade.
- Sidebar do administrador compacta em tablet e horizontal no celular.
- Datas em um card próprio, com atalhos Hoje, 7 dias, Este mês e 30 dias.
- Validação para impedir período final anterior ao inicial.
- Cards de métricas com melhor hierarquia e indicadores de retirada e entrega.
- Tabelas administrativas transformadas em cards no celular.
- Ação rápida para marcar ou retirar produto de Novidades.
- Selos de Oferta e Novidade na gestão e no catálogo.
- Páginas Ofertas e Novidades ligadas aos filtros reais da API, com carregamento, erro e lista vazia.
- Percentual da oferta calculado corretamente quando o desconto é cadastrado em reais.
- Carga de homologação corrigida para restaurar perfil ADMIN/COLABORADOR e status ativo/inativo mesmo quando os registros já existem.
- Card da sacola redesenhado com imagem real do produto e imagem reserva.
- Resumo de finalização reorganizado, com seletores e campos ocupando a largura correta.
- Endereço de entrega responsivo, sem campos escapando do card.
- Confirmação antes de limpar toda a sacola.
- Remoção total e individual corrigida para retirar os itens também da resposta em memória.
- Teste de regressão cobrindo adicionar produto e limpar a sacola.
- Catálogo FTW salvo no projeto e importado de forma idempotente, sem sobrescrever produtos existentes.
- Produtos importados preservados em modo rascunho: inativos, sem preço e com estoque zero.
- Ativação bloqueada no backend enquanto preço ou estoque não forem preenchidos.
- Fluxo de recuperação de senha implementado sem revelar se um e-mail existe, armazenando apenas o hash do token.
- Tomcat validado em execução na porta 5001 com `Http11Nio2Protocol`.

## Pendências externas

- O envio real da recuperação de senha aguarda as credenciais do provedor SMTP; os endpoints e as telas já estão implementados.
- O FigJam aguarda a conexão da conta Figma no Codex.

## Critérios de conclusão por bloco

Cada bloco somente muda para “Concluído” depois de:

1. Implementação no front-end e, quando necessário, no backend.
2. Build do React sem erro.
3. Testes Java sem falhas.
4. Inspeção visual em desktop e celular.
5. Atualização deste acompanhamento.
