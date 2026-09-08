// Curadoria editorial configuravel. Metricas, ofertas e ranking vêm exclusivamente da API.

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
