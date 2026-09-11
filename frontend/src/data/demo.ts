// Curadoria editorial configuravel. Metricas, ofertas e ranking vêm exclusivamente da API.

export const campaigns = [
  { eyebrow: 'Seu objetivo. Seu ritmo.', title: 'EMAGRECER', text: 'Escolhas inteligentes para transformar constância em resultado.', to: '/catalogo?objetivos=EMAGRECIMENTO', image: '/assets/imagens/banner-foto-1.jpg' },
  { eyebrow: 'Força para evoluir', title: 'GANHAR MASSA', text: 'Nutrição e performance para construir sua melhor versão.', to: '/catalogo?objetivos=GANHO_DE_MASSA', image: '/assets/imagens/imagem-com-fundo-chat.categorias.png' },
  { eyebrow: 'Vá mais longe', title: 'AUMENTAR A PERFORMANCE', text: 'Energia, foco e recuperação para superar seus limites.', to: '/catalogo?objetivos=PERFORMANCE', image: '/assets/imagens/wheyBanner' },
]

export const goals = [
  ['EMAGRECIMENTO', 'Emagrecer'], ['GANHO_DE_MASSA', 'Ganhar massa'],
  ['PERFORMANCE', 'Melhorar performance'], ['SAUDE_E_BEM_ESTAR', 'Saúde e bem-estar'],
]

export const sports = [
  ['MUSCULACAO', 'Musculação', '/assets/imagens/musculacao'], ['CORRIDA', 'Corrida', '/assets/imagens/corrida'],
  ['FUTEBOL', 'Futebol', '/assets/imagens/futebol'], ['PILATES', 'Pilates', '/assets/imagens/pilates.jpg'],
  ['CICLISMO', 'Ciclismo', '/assets/imagens/banner-foto-1.jpg'], ['CROSS_TRAINING', 'Cross training', '/assets/imagens/imagem-com-fundo-chat.categorias.png'],
]
