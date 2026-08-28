const FAVORITES = 'vita-fortis-favorites'
const RECENT = 'vita-fortis-recent'
const read = (key: string): number[] => { try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] } }
export const favoriteIds = () => read(FAVORITES)
export const toggleFavorite = (id: number) => { const values = read(FAVORITES); const next = values.includes(id) ? values.filter((item) => item !== id) : [id, ...values]; localStorage.setItem(FAVORITES, JSON.stringify(next)); window.dispatchEvent(new Event('vita-favorites')); return next.includes(id) }
export const addRecentlyViewed = (id: number) => localStorage.setItem(RECENT, JSON.stringify([id, ...read(RECENT).filter((item) => item !== id)].slice(0, 8)))
export const recentlyViewedIds = () => read(RECENT)
