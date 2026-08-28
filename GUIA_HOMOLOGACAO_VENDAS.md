# Guia de homologacao de vendas sem gateway

Este documento descreve como testar o fluxo comercial completo da Vita Fortis usando pagamento local simulado. Nenhum pagamento real e realizado e nenhum dado de cartao e armazenado.

## Preparacao

1. Inicie o MySQL configurado em `application.properties`.
2. Gere os dados idempotentes de homologacao executando os testes:

   ```powershell
   .\mvnw.cmd test
   ```

3. Compile o frontend e copie o bundle para o Spring Boot conforme o fluxo do projeto.
4. Inicie a aplicacao normalmente em `http://localhost:5001/`.

O perfil `homologacao` possui um inicializador idempotente. Ele cria somente registros identificados pelos e-mails, codigos e cupom abaixo; repeticoes nao duplicam os dados.

## Contas de homologacao

Todas utilizam a senha `Teste@123`.

| Perfil | E-mail | Finalidade |
|---|---|---|
| ADMIN | `admin@vitafortis.test` | Aprovar/recusar pagamento, atualizar pedidos e consultar o painel |
| COLABORADOR | `colaborador@vitafortis.test` | Consultar cupom, vendas, saldo e extrato de cashback |
| CLIENTE | `cliente@vitafortis.test` | Executar a compra principal |
| CLIENTE | `cliente2@vitafortis.test` | Testar isolamento entre contas |

## Dados preparados

- `HML-WHEY`: produto ativo com estoque normal.
- `HML-CREATINA`: produto ativo com estoque baixo.
- `HML-ZERO`: produto ativo sem estoque.
- `HML-INATIVO`: produto inativo, visivel apenas no painel administrativo.
- `COLAB10`: 10% de desconto, vinculado ao colaborador, gerando 5% de cashback.

## Roteiro principal de venda

1. Entre como `cliente@vitafortis.test`.
2. Abra o catalogo e acesse os detalhes de `HML-WHEY`.
3. Adicione uma ou mais unidades a sacola.
4. Aplique o cupom `COLAB10`.
5. Confirme que subtotal, desconto, total e cashback do colaborador aparecem separados.
6. Escolha retirada ou entrega e uma forma de pagamento simulada.
7. Clique em **Revisar pedido**.
8. Confira a revisao final e clique em **Confirmar pedido**.
9. Verifique em Meus pedidos o estado `PENDENTE` e pagamento `PENDENTE`, incluindo a referencia mascarada.
10. Saia e entre como administrador.
11. No painel de pedidos, aprove o pagamento simulado.
12. Confirme que o pedido mudou para `PAGAMENTO_APROVADO`.
13. Avance na ordem: `EM_SEPARACAO`, `ENVIADO` e `ENTREGUE`.
14. Entre como colaborador e confira vendas, cupom e credito no extrato.

## Cenários obrigatorios

### Pagamento recusado

- Crie outro pedido como cliente.
- No painel administrativo use **Recusar**.
- O pedido deve ser cancelado, o pagamento deve ficar `RECUSADO` e o estoque deve retornar.

### Cancelamento apos aprovacao

- Aprove um pedido e depois cancele antes da entrega.
- O estoque e o total vendido devem ser revertidos.
- O cashback deve receber um unico movimento de estorno.

### Estoque

- `HML-ZERO` nao pode ser adicionado.
- Tentar comprar mais que o estoque de `HML-CREATINA` deve falhar com mensagem clara.
- `HML-INATIVO` nao deve aparecer no catalogo publico.

### Cupons

- `COLAB10` deve aplicar desconto e informar o cashback do colaborador.
- Codigo inexistente, inativo ou vencido deve ser rejeitado.

### Seguranca

- `cliente2@vitafortis.test` nao pode consultar ou alterar o carrinho do primeiro cliente.
- Um cliente nao pode consultar pedidos de outro cliente.
- Um colaborador nao pode consultar pedidos arbitrarios de clientes.
- COLABORADOR nao pode acessar endpoints administrativos.
- CLIENTE nao pode acessar a area do colaborador.

### Responsividade

- Repetir catalogo, produto, sacola, revisao e pedidos em largura de celular e desktop.
- Conferir menu, tabelas, formularios, mensagens, estados vazios e botoes desabilitados.

## Regras implementadas

- Carrinho, favoritos e enderecos sao protegidos por proprietario.
- Pedidos proprios sao acessiveis ao cliente; somente ADMIN pode consultar pedidos de terceiros.
- COLABORADOR recebe somente os pedidos associados aos seus cupons na area propria.
- Pedido pendente nao pode ir diretamente para separacao.
- Aprovar ou recusar o mesmo pagamento duas vezes e bloqueado.
- Recusa cancela o pedido e devolve o estoque.
- Aprovacao gera cashback; cancelamento posterior gera estorno auditavel.
- A confirmacao do pedido exige uma etapa explicita de revisao no frontend.

## Melhorias posteriores

- Substituir o pagamento local pelo gateway real e webhook idempotente.
- Automatizar expiracao de pedidos pendentes e liberacao de estoque.
- Adicionar testes de navegador automatizados para o roteiro completo.
- Isolar a homologacao em banco proprio, em vez de usar a mesma conexao local.
- Adicionar migracoes versionadas, e-mail transacional, recuperacao de senha, logs operacionais e observabilidade.
- Definir a politica comercial final de reserva de estoque e de prazo para pagamento.
