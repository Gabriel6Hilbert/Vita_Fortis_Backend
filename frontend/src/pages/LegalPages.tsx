import { Link, useLocation } from 'react-router-dom'

type LegalSection = { title: string; paragraphs?: string[]; items?: string[] }
type LegalDocument = { title: string; eyebrow: string; intro: string; pending?: string; sections: LegalSection[] }

const company = {
  legalName: '61.156.510 Myllena dos Santos Pereira de Freitas',
  tradeName: 'Vita Fortis',
  cnpj: '61.156.510/0001-47',
  address: 'Avenida Waldyr Soeiro Emrich, 5230, Bairro Diamante (Barreiro), Belo Horizonte/MG, CEP 30660-560',
  supportEmail: 'lojavitafortis@gmail.com',
  privacyEmail: 'vitafortis@atendimentos.com.br',
  phone: '(31) 9 8898-7188',
}

const legalLinks = [
  { to: '/politica-de-privacidade', label: 'Política de Privacidade' },
  { to: '/politica-de-cookies', label: 'Política de Cookies' },
  { to: '/termos-de-uso-e-compra', label: 'Termos de Uso e Compra' },
  { to: '/entrega-e-retirada', label: 'Entrega e Retirada' },
  { to: '/trocas-devolucoes-e-arrependimento', label: 'Trocas, Devoluções e Arrependimento' },
]

const documents: Record<string, LegalDocument> = {
  '/politica-de-privacidade': {
    title: 'Política de Privacidade', eyebrow: 'Privacidade e LGPD',
    intro: 'Esta minuta explica como a Vita Fortis trata dados pessoais no cadastro, atendimento, navegação e processamento de pedidos.',
    pending: 'Os prazos de retenção abaixo são uma proposta operacional e devem ser validados pelo responsável jurídico antes da publicação definitiva.',
    sections: [
      { title: 'Controlador e contato', paragraphs: [
        `${company.tradeName}, nome empresarial ${company.legalName}, inscrita no CNPJ sob o nº ${company.cnpj}, com endereço em ${company.address}, é responsável pelas decisões sobre o tratamento descrito nesta política.`,
        `O canal para dúvidas e solicitações relacionadas à privacidade é ${company.privacyEmail}. O responsável pelo canal de privacidade é a Vita Fortis.`,
      ] },
      { title: 'Dados tratados', items: [
        'Dados de cadastro e contato, como nome, CPF, e-mail e telefone.',
        'Endereços e informações necessárias à entrega ou retirada.',
        'Dados do pedido, produtos, valores, descontos, forma de pagamento e histórico de atendimento.',
        'Preferências de comunicação, favoritos e itens visualizados recentemente.',
        'Registros técnicos de sessão, segurança e prevenção a acessos indevidos.',
        'Protocolos, decisões e auditorias das solicitações de privacidade.',
      ] },
      { title: 'Finalidades', items: [
        'Criar e administrar a conta do cliente.',
        'Processar pedidos, pagamentos, entregas, retiradas, trocas e devoluções.',
        'Prestar atendimento e enviar comunicações necessárias ao pedido.',
        'Cumprir obrigações legais, fiscais, regulatórias e exercer direitos em processos.',
        'Proteger contas, sessões e operações contra fraude e uso indevido.',
        'Atender solicitações de acesso, correção e exclusão de dados.',
        'Enviar comunicações promocionais somente quando houver autorização aplicável.',
      ] },
      { title: 'Bases legais propostas para revisão', paragraphs: [
        'Os tratamentos poderão se apoiar na execução de contrato ou de procedimentos preliminares, no cumprimento de obrigação legal ou regulatória, no exercício regular de direitos, na proteção contra fraude e, quando aplicável, no consentimento. A vinculação final de cada operação à sua base legal consta do inventário interno e depende de revisão jurídica.',
      ] },
      { title: 'Compartilhamento', paragraphs: [
        'Os dados poderão ser compartilhados, no limite necessário, com fornecedores de hospedagem e infraestrutura, meios de pagamento, entrega, atendimento e e-mail. A lista final de operadores e eventual transferência internacional será confirmada antes da publicação.',
      ] },
      { title: 'Retenção e eliminação', items: [
        'Conta e cadastro: enquanto ativos; dados não sujeitos a obrigação serão eliminados ou anonimizados em até 12 meses após o encerramento.',
        'Pedidos, referências de pagamento e solicitações de privacidade: em regra, 5 anos para obrigações e exercício de direitos, salvo prazo maior aplicável ao caso.',
        'Tokens de recuperação de senha: validade de 30 minutos e exclusão automática 24 horas após a expiração.',
        'Registros de acesso, quando coletados: 6 meses em ambiente controlado e seguro.',
        'Favoritos e sacola: até 12 meses de inatividade ou exclusão pelo usuário.',
        'Backups: rotação de 7 cópias diárias e 4 semanais, seguida de eliminação segura.',
      ] },
      { title: 'Direitos do titular', paragraphs: [
        `O titular pode utilizar a área da conta ou escrever para ${company.privacyEmail} para solicitar confirmação, acesso, correção ou exclusão. A exclusão poderá ser limitada quando a conservação for necessária para obrigação legal, regulatória ou exercício de direitos.`,
      ] },
      { title: 'Crianças e adolescentes', paragraphs: [
        'Menores de 18 anos podem realizar pedidos somente com conhecimento e autorização de seus pais ou responsáveis legais. O tratamento de seus dados deve observar seu melhor interesse e as proteções específicas aplicáveis.',
      ] },
    ],
  },
  '/politica-de-cookies': {
    title: 'Política de Cookies', eyebrow: 'Tecnologias do navegador',
    intro: 'Esta minuta descreve as tecnologias necessárias ao funcionamento atual da loja.',
    pending: 'A decisão sobre Meta Pixel, Google Analytics e cookies de publicidade permanece em stand-by. Nenhuma dessas tecnologias foi identificada no código atual.',
    sections: [
      { title: 'Tecnologias essenciais', items: [
        'Cookie de sessão e mecanismo antifalsificação, usados para autenticação e segurança das operações.',
        'Armazenamento de sessão, usado para manter a sessão e a sacola temporária no navegador.',
        'Armazenamento local, usado para favoritos e produtos visualizados recentemente.',
      ] },
      { title: 'Controle pelo usuário', paragraphs: ['O navegador permite apagar ou bloquear cookies e dados locais. O bloqueio de tecnologias essenciais pode impedir login, sacola, checkout e outras funções da loja.'] },
      { title: 'Publicidade e medição', paragraphs: ['Caso tecnologias analíticas ou publicitárias sejam adotadas, esta política deverá ser atualizada antes da ativação, com informação sobre fornecedor, finalidade, duração e mecanismo de escolha do usuário.'] },
    ],
  },
  '/termos-de-uso-e-compra': {
    title: 'Termos de Uso e Compra', eyebrow: 'Regras da loja',
    intro: 'Estes termos organizam o uso da loja e as principais etapas de uma compra realizada no site da Vita Fortis.',
    sections: [
      { title: 'Identificação e atendimento', paragraphs: [`${company.tradeName}, nome empresarial ${company.legalName}, CNPJ ${company.cnpj}. Atendimento pelo e-mail ${company.supportEmail} e telefone ou WhatsApp ${company.phone}.`] },
      { title: 'Cadastro e responsabilidade', items: [
        'O cliente deve fornecer dados verdadeiros, completos e atualizados.',
        'As credenciais de acesso são pessoais e devem ser protegidas pelo titular.',
        'Pedidos de menores de 18 anos exigem conhecimento e autorização de seus pais ou responsáveis legais.',
      ] },
      { title: 'Formação do pedido', paragraphs: ['Antes da confirmação, o site apresenta produtos, quantidades, descontos, frete, prazo estimado, forma de recebimento e total. A conclusão depende da disponibilidade do produto e da confirmação do pagamento quando aplicável.'] },
      { title: 'Preços e pagamento', paragraphs: ['Prevalecem os preços e condições apresentados na revisão do pedido. As formas de pagamento disponíveis são exibidas no checkout. A Vita Fortis não solicita o envio de número completo de cartão ou código de segurança por e-mail ou WhatsApp.'] },
      { title: 'Documentos relacionados', paragraphs: ['As regras específicas de entrega, retirada, privacidade, cookies, trocas, devoluções e arrependimento integram estes termos e devem ser consultadas nos respectivos documentos.'] },
    ],
  },
  '/entrega-e-retirada': {
    title: 'Política de Entrega e Retirada', eyebrow: 'Recebimento do pedido',
    intro: 'Esta minuta reúne as condições atualmente confirmadas para entrega e retirada de pedidos.',
    pending: 'O prazo específico de entrega em Belo Horizonte e municípios da região ainda precisa de definição comercial antes da aprovação final.',
    sections: [
      { title: 'Área atendida e prazo', paragraphs: ['A entrega está disponível para endereços em Minas Gerais, São Paulo, Rio de Janeiro e Espírito Santo. A preparação pode levar até 24 horas. Fora de Belo Horizonte e região, o prazo informado atualmente é de até cinco dias úteis após a preparação, sujeito à confirmação no checkout.'] },
      { title: 'Belo Horizonte e região', paragraphs: ['A região informada pela Vita Fortis inclui Belo Horizonte, Baldim, Betim, Brumadinho, Caeté, Capim Branco, Contagem, Esmeraldas, Florestal, Ibirité, Igarapé, Itaguara, Itatiaiuçu, Jaboticatubas, Juatuba, Lagoa Santa, Mário Campos, Mateus Leme, Matozinhos, Nova Lima, Nova União, Pedro Leopoldo, Raposos, Ribeirão das Neves, Rio Acima, Rio Manso, Sabará, Santa Luzia, São Joaquim de Bicas, São José da Lapa, Sarzedo, Taquaraçu de Minas e Vespasiano.'] },
      { title: 'Frete', paragraphs: ['O frete é grátis quando o valor dos produtos, após os descontos, for superior a R$ 200,00. Para os demais pedidos, o valor e o prazo são apresentados na revisão do checkout antes da confirmação.'] },
      { title: 'Entrega própria e acompanhamento', paragraphs: ['A entrega é realizada pela própria equipe Vita Fortis, sem transportadora externa. As atualizações ficam disponíveis no histórico do pedido e a equipe poderá confirmar informações operacionais pelo WhatsApp cadastrado, usando os canais oficiais.'] },
      { title: 'Retirada', paragraphs: [
        `A retirada não possui cobrança de frete e ocorre em ${company.address}. O ponto de referência é ao lado da Academia Smart Fit.`,
        'Horários: de segunda a sexta-feira, das 8h às 20h; aos sábados, das 9h às 14h. O pedido pode levar até 24 horas para preparação e deve ser retirado após a confirmação de disponibilidade.',
      ] },
      { title: 'Imprevistos', paragraphs: ['Endereço incorreto ou incompleto, ausência de responsável, eventos climáticos, acidentes, greves e outras situações fora do controle da loja podem alterar o prazo. A Vita Fortis utilizará os canais cadastrados para orientar o cliente quando necessário.'] },
    ],
  },
  '/trocas-devolucoes-e-arrependimento': {
    title: 'Trocas, Devoluções e Arrependimento', eyebrow: 'Atendimento pós-compra',
    intro: 'Esta minuta diferencia arrependimento de compra on-line, troca voluntária e atendimento de produto com defeito ou divergência.',
    sections: [
      { title: 'Como solicitar', paragraphs: [`Envie a solicitação para ${company.supportEmail}, informando o número do pedido, o produto e o motivo. Aguarde as instruções antes de realizar qualquer postagem.`] },
      { title: 'Arrependimento em compra on-line', paragraphs: ['O cliente pode desistir da compra em até sete dias corridos contados do recebimento. O atendimento observará a devolução integral dos valores pagos, inclusive o frete original quando aplicável, após o retorno e a conferência dos produtos, sem limitar os direitos previstos na legislação.'] },
      { title: 'Condições de devolução', paragraphs: ['Sempre que possível, o produto deve ser devolvido na embalagem original, com acessórios e documentos disponíveis. A ausência de embalagem ou documento fiscal será analisada conforme o motivo da solicitação e não elimina direitos legais relacionados a defeito, divergência ou arrependimento.'] },
      { title: 'Suplementos abertos ou consumidos', paragraphs: ['Por razões sanitárias, a loja poderá recusar troca voluntária de suplemento aberto ou consumido. Essa limitação não afasta o atendimento de defeito, vício, divergência, produto impróprio ou outros direitos previstos em lei.'] },
      { title: 'Produto com defeito ou divergente', paragraphs: ['Produtos com possível defeito, avaria, divergência ou envio incorreto serão analisados pela Vita Fortis. Confirmado o problema, serão aplicadas as alternativas cabíveis previstas na legislação, incluindo substituição, restituição ou abatimento quando aplicável.'] },
      { title: 'Troca voluntária', paragraphs: ['Trocas por preferência do cliente dependem da integridade do produto e da disponibilidade de estoque. Eventuais custos de uma segunda troca voluntária do mesmo pedido poderão ser atribuídos ao cliente, desde que isso não envolva defeito, erro da loja ou exercício de direito legal.'] },
      { title: 'Restituição', paragraphs: ['A restituição será encaminhada pelo meio compatível com o pagamento. A Vita Fortis processará a solicitação após a conferência; o prazo de visualização em cartão também depende da administradora. Vale-troca será oferecido como opção, não como substituição obrigatória do reembolso devido.'] },
    ],
  },
}

function PolicyIndex() {
  return <div className="page container narrow legal-page">
    <header className="page-header"><span className="eyebrow">Transparência</span><h1>Políticas da Vita Fortis</h1><p>Documentos estruturados para consulta antes da aprovação e publicação definitiva.</p></header>
    <div className="legal-pending" role="note"><strong>Minutas em revisão.</strong> Retenção de dados, tecnologias de publicidade e validação jurídica permanecem pendentes.</div>
    <nav className="legal-index" aria-label="Documentos jurídicos">{legalLinks.map((item) => <Link key={item.to} to={item.to}>{item.label}<span>Consultar minuta</span></Link>)}</nav>
  </div>
}

export function LegalPage() {
  const { pathname } = useLocation()
  if (pathname === '/politicas') return <PolicyIndex />
  const document = documents[pathname]
  if (!document) return <PolicyIndex />
  return <div className="page container narrow legal-page">
    <header className="page-header"><span className="eyebrow">{document.eyebrow}</span><h1>{document.title}</h1><p>{document.intro}</p></header>
    {document.pending && <div className="legal-pending" role="note"><strong>Pendência antes da aprovação.</strong> {document.pending}</div>}
    <article className="prose-card legal-document">{document.sections.map((section) => <section key={section.title}><h2>{section.title}</h2>{section.paragraphs?.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}{section.items && <ul>{section.items.map((item) => <li key={item}>{item}</li>)}</ul>}</section>)}</article>
    <nav className="legal-related" aria-label="Outras políticas"><Link to="/politicas">Ver todas as políticas</Link></nav>
  </div>
}
