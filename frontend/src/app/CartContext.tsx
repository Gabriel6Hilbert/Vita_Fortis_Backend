import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../services/api'
import type { Cart } from '../types/api'
import { useAuth } from './AuthContext'

type CartContextValue = { cart: Cart | null; loading: boolean; error: string; count: number; total: number; refresh: () => Promise<void>; add: (productId: number, quantity?: number) => Promise<void>; update: (itemId: number, quantity: number) => Promise<void>; remove: (itemId: number) => Promise<void>; clear: () => Promise<void> }
const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [cart, setCart] = useState<Cart | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const refresh = useCallback(async () => { if (!user || user.tipoUsuario !== 'CLIENTE') { setCart(null); setError(''); return }; setLoading(true); setError(''); try { setCart(await api.cart(user.id)) } catch (cause) { setCart(null); setError(cause instanceof Error ? cause.message : 'Não foi possível carregar sua sacola.') } finally { setLoading(false) } }, [user])
  useEffect(() => { void refresh() }, [refresh])
  const requireUser = () => { if (!user) throw new Error('Faça login para usar sua sacola.'); if (user.tipoUsuario !== 'CLIENTE') throw new Error('A sacola está disponível somente para clientes.'); return user }
  const run = async (action: (id: number) => Promise<Cart>) => { setLoading(true); setError(''); try { setCart(await action(requireUser().id)) } catch (cause) { const message=cause instanceof Error ? cause.message : 'Não foi possível atualizar sua sacola.'; setError(message); throw cause } finally { setLoading(false) } }
  const value = useMemo<CartContextValue>(() => ({ cart, loading, error, count: cart?.carrinhoItens.reduce((sum, item) => sum + item.quantidade, 0) || 0, total: Number(cart?.total ?? 0), refresh, add: (productId, quantity = 1) => run((id) => api.addCart(id, productId, quantity)), update: (itemId, quantity) => run((id) => api.updateCart(id, itemId, quantity)), remove: (itemId) => run((id) => api.removeCart(id, itemId)), clear: () => run((id) => api.clearCart(id)) }), [cart, loading, error, refresh])
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
export const useCart = () => { const context = useContext(CartContext); if (!context) throw new Error('CartProvider ausente'); return context }
