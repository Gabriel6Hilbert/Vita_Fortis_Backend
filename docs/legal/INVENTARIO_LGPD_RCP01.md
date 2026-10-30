# Inventário LGPD RCP 01

Revisão técnica: 28 de outubro de 2026.

Este documento é uma minuta técnica baseada nos fluxos existentes no repositório. As bases legais e os prazos abaixo são uma proposta operacional para validação jurídica, não parecer jurídico.

## Identificação do controlador

- Nome empresarial: 61.156.510 Myllena dos Santos Pereira de Freitas
- Nome fantasia: Vita Fortis
- CNPJ: 61.156.510/0001-47
- Endereço: Avenida Waldyr Soeiro Emrich, 5230, Bairro Diamante (Barreiro), Belo Horizonte/MG, CEP 30660-560
- Canal de privacidade: vitafortis@atendimentos.com.br
- Responsável informado pelo canal: Vita Fortis

## Inventário de tratamentos

| Tratamento | Dados principais | Finalidade | Base legal proposta | Compartilhamento a confirmar | Retenção |
|---|---|---|---|---|---|
| Cadastro e conta | nome, CPF, e-mail, telefone, credencial protegida e perfil | criar conta, autenticar e administrar acesso | execução de contrato ou procedimento preliminar; proteção contra fraude | hospedagem e infraestrutura | enquanto a conta estiver ativa; após encerramento, eliminar ou anonimizar dados não vinculados a obrigação em até 12 meses |
| Endereços | CEP, logradouro, número, complemento, bairro, cidade e UF | calcular frete e realizar entrega própria | execução de contrato | equipe Vita Fortis e hospedagem | endereços avulsos: até encerramento da conta; endereço que integra pedido: mesmo prazo do pedido |
| Sacola e favoritos | produtos, quantidades, favoritos e itens recentes | manter seleção e preparar compra | procedimento preliminar; legítimo interesse sujeito a revisão | hospedagem; armazenamento local do navegador | até 12 meses de inatividade ou exclusão pelo usuário |
| Pedidos | itens, valores, descontos, frete, endereço, recebimento e histórico | executar compra, entrega, retirada, troca e atendimento | execução de contrato; obrigação legal; exercício regular de direitos | pagamento, entrega própria, hospedagem e atendimento | 5 anos após a conclusão ou cancelamento, salvo prazo legal ou processo mais longo |
| Pagamento | forma, estado e referência da operação | processar, conciliar, estornar e prevenir fraude | execução de contrato; obrigação legal; proteção ao crédito ou fraude, conforme revisão | gateway de pagamento ainda não contratado | 5 anos; a Vita Fortis não deve armazenar número completo do cartão ou CVV |
| Comunicações | e-mail, telefone e opção de comunicações | mensagens do pedido e comunicações promocionais autorizadas | execução de contrato para mensagens necessárias; consentimento para publicidade | Brevo após contratação e autenticação | transacionais: prazo do pedido; promocionais: até revogação, com bloqueio imediato e prova da escolha por 5 anos |
| Recuperação de senha | usuário, hash do token, criação, expiração e uso | recuperar acesso com segurança | execução de contrato; legítimo interesse sujeito a revisão | Brevo após contratação | token válido por 30 minutos; registro excluído automaticamente 24 horas após expiração |
| Solicitações LGPD | tipo, descrição, protocolo, decisão, justificativa, responsável e auditoria | exercer direitos e comprovar atendimento | cumprimento de obrigação legal; exercício regular de direitos | equipe administrativa autorizada | 5 anos após encerramento da solicitação |
| Segurança e sessão | sessão, token CSRF e registros técnicos disponíveis | autenticar, proteger operações e prevenir uso indevido | legítimo interesse e proteção contra fraude sujeitos a revisão | hospedagem e infraestrutura | sessão até logout/expiração; registros de acesso, quando coletados, por 6 meses |
| Avaliações | usuário, nota, comentário e data | publicar e moderar avaliação de produto | consentimento ou legítimo interesse, a definir com jurídico | publicação no site quando aprovada | enquanto publicada ou até retirada; prova de moderação por até 5 anos quando necessária |
| Backups | cópia dos dados persistidos | continuidade, recuperação de desastre e segurança | obrigação legal, execução de contrato e legítimo interesse sujeitos a revisão | armazenamento externo a contratar | 7 diários e 4 semanais; eliminação segura ao final da rotação |

## Crianças e adolescentes

A regra comercial informada permite pedidos de menores de 18 anos somente com conhecimento e autorização de pais ou responsáveis legais. Antes da aprovação jurídica, ainda devem ser definidos o mecanismo de declaração ou verificação aplicável e o tratamento específico para crianças, sempre observando o melhor interesse.

## Tecnologias no navegador

O código atual utiliza sessão, proteção CSRF, armazenamento de sessão para autenticação e sacola temporária, e armazenamento local para favoritos e itens recentes. Não foram identificados Google Analytics, Meta Pixel ou cookies de publicidade. A decisão sobre adoção futura dessas tecnologias permanece em stand-by.

## Pendências para aprovação

1. Validar juridicamente a tabela de retenção proposta acima.
2. Confirmar as bases legais com responsável jurídico.
3. Confirmar fornecedores e operadores efetivos, incluindo hospedagem, pagamento, e-mail e entrega.
4. Avaliar eventual transferência internacional de dados pelos fornecedores contratados.
5. Definir o mecanismo aplicável aos pedidos realizados por menores.
6. Decidir sobre Analytics, Meta Pixel e cookies de publicidade antes de qualquer ativação.
