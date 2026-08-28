// Dados exclusivamente demonstrativos. Remover quando os endpoints de analytics/campanhas existirem.
export const adminDemo = {
  metrics: [
    { label: 'Faturamento hoje', value: 'R$ 2.847,60', change: '+12,4%' },
    { label: 'Faturamento no mês', value: 'R$ 48.392,10', change: '+8,7%' },
    { label: 'Pedidos', value: '186', change: '+14' },
    { label: 'Ticket médio', value: 'R$ 260,17', change: '+3,1%' },
    { label: 'Produtos vendidos', value: '429', change: '+9,8%' },
    { label: 'Novos clientes', value: '37', change: '+6' },
  ],
  sales7d: [42, 58, 51, 74, 68, 91, 83],
  days: ['Qua', 'Qui', 'Sex', 'Sáb', 'Dom', 'Seg', 'Ter'],
  categories: [
    { name: 'Proteínas', value: 38 }, { name: 'Aminoácidos', value: 24 },
    { name: 'Vitaminas', value: 18 }, { name: 'Outros', value: 20 },
  ],
}

export const campaigns = [
  { eyebrow: 'Semana Vita', title: 'Energia para ir além.', text: 'Seleção promocional para acompanhar sua rotina.', to: '/catalogo?oferta=true', image: '/assets/imagens/banner-foto-1.jpg' },
  { eyebrow: 'Ganho de massa', title: 'Evolução com estratégia.', text: 'Uma curadoria para força, recuperação e consistência.', to: '/catalogo?objetivos=GANHO_DE_MASSA', image: '/assets/imagens/imagem-com-fundo-chat.categorias.png' },
]

export const goals = [
  ['maisVendidos', 'Mais vendidos'], ['GANHO_DE_MASSA', 'Ganho de massa'], ['EMAGRECIMENTO', 'Emagrecimento'],
  ['DEFINICAO_MUSCULAR', 'Definição muscular'], ['ENERGIA', 'Energia'], ['RECUPERACAO', 'Recuperação'], ['BEM_ESTAR', 'Saúde e bem-estar'],
]

export const sports = [
  ['MUSCULACAO', 'Musculação', '/assets/imagens/musculacao'], ['CORRIDA', 'Corrida', '/assets/imagens/corrida'],
  ['FUTEBOL', 'Futebol', '/assets/imagens/futebol'], ['PILATES', 'Pilates', '/assets/imagens/pilates.jpg'],
  ['CICLISMO', 'Ciclismo', '/assets/imagens/banner-foto-1.jpg'], ['CROSS_TRAINING', 'Cross training', '/assets/imagens/imagem-com-fundo-chat.categorias.png'],
]
