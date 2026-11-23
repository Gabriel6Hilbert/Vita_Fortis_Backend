import type { HomeSection } from '../types/api'

export const homeContent = {
  video: {
    eyebrow: 'Movimento Vita Fortis',
    title: 'Evolução também é constância.',
    description: 'Um espaço preparado para receber histórias, treinos e escolhas que acompanham diferentes ritmos.',
    poster: '/assets/imagens/hero-performance-desktop.webp',
    source: null as string | null,
    placeholderLabel: 'Conteúdo audiovisual em preparação',
  },
  institutional: {
    eyebrow: 'Vita Fortis',
    // Texto provisório: manter centralizado aqui até a aprovação institucional do proprietário.
    statement: 'Frase institucional provisória — aguardando aprovação',
    image: '/assets/imagens/banner-foto-1.jpg',
    linkLabel: 'Conheça a Vita Fortis',
    linkTo: '/sobre',
  },
} as const

export const defaultHomeSections: HomeSection[] = [
  { chave: 'HERO', ativa: true, ordem: 10, modo: 'DINAMICO', limite: 3, produtoIds: [] },
  { chave: 'OBJETIVOS', ativa: true, ordem: 20, modo: 'DINAMICO', titulo: 'EU QUERO...', subtitulo: 'Escolha seu objetivo e encontre produtos relacionados.', limite: 4, produtoIds: [] },
  { chave: 'OFERTAS', ativa: true, ordem: 30, modo: 'DINAMICO', titulo: 'Ofertas para aproveitar agora', limite: 4, produtoIds: [] },
  { chave: 'MAIS_VENDIDOS', ativa: true, ordem: 40, modo: 'DINAMICO', titulo: 'Mais vendidos', subtitulo: 'Os produtos mais escolhidos na Vita Fortis.', limite: 8, produtoIds: [] },
  { chave: 'DESTAQUES', ativa: true, ordem: 50, modo: 'DINAMICO', titulo: 'Produtos em destaque', limite: 4, produtoIds: [] },
  { chave: 'VIDEO', ativa: true, ordem: 60, modo: 'DINAMICO', titulo: homeContent.video.title, subtitulo: homeContent.video.description, mediaUrl: homeContent.video.poster, texto: homeContent.video.placeholderLabel, limite: 1, produtoIds: [] },
  { chave: 'LINHAS', ativa: true, ordem: 70, modo: 'DINAMICO', titulo: 'Uma Linha, Uma Função', limite: 5, produtoIds: [] },
  { chave: 'ATALHOS', ativa: true, ordem: 80, modo: 'DINAMICO', limite: 4, produtoIds: [] },
  { chave: 'INSTITUCIONAL', ativa: true, ordem: 90, modo: 'DINAMICO', titulo: homeContent.institutional.statement, mediaUrl: homeContent.institutional.image, limite: 1, produtoIds: [] },
]
