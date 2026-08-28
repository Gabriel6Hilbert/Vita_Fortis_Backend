import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../services/api'
import type { Cart } from '../types/api'
import { useAuth } from './AuthContext'

type CartContextValue = { cart: Cart | null; loading: boolean; count: number; total: number; refresh: () => Promise<void>; add: (productId: number, quantity?: number) => Promise<void>; update: (itemId: number, quantity: number) => Promise<void>; remove: (itemId: number) => Promise<void>; clear: () => Promise<void> }
const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [loading, setLoading] = useState(false)
  const refresh = useCallback(async () => { if (!user) { setCart(null); return }; setLoading(true); try { setCart(await api.cart(user.id)) } finally { setLoading(false) } }, [user])
  useEffect(() => { void refresh() }, [refresh])
  const requireUser = () => { if (!user) throw new Error('Faça login para usar sua sacola.'); return user }
  const run = async (action: (id: number) => Promise<Cart>) => { setLoading(true); try { setCart(await action(requireUser().id)) } finally { setLoading(false) } }
  const value = useMemo<CartContextValue>(() => ({ cart, loading, count: cart?.carrinhoItens.reduce((sum, item) => sum + item.quantidade, 0) || 0, total: cart?.carrinhoItens.reduce((sum, item) => sum + Number(item.subtotal), 0) || 0, refresh, add: (productId, quantity = 1) => run((id) => api.addCart(id, productId, quantity)), update: (itemId, quantity) => run((id) => api.updateCart(id, itemId, quantity)), remove: (itemId) => run((id) => api.removeCart(id, itemId)), clear: () => run((id) => api.clearCart(id)) }), [cart, loading, refresh])
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
export const useCart = () => { const context = useContext(CartContext); if (!context) throw new Error('CartProvider ausente'); return context }
