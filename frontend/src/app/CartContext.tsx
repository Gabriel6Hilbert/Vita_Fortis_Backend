import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../services/api'
import type { Cart, CartItem } from '../types/api'
import { useAuth } from './AuthContext'

const GUEST_CART='vita-fortis-guest-cart'
const readGuest=():CartItem[]=>{try{return JSON.parse(sessionStorage.getItem(GUEST_CART)||'[]')}catch{return []}}
const saveGuest=(items:CartItem[])=>{sessionStorage.setItem(GUEST_CART,JSON.stringify(items));window.dispatchEvent(new Event('vita-cart'))}
const guestCart=(items:CartItem[]):Cart=>{const subtotal=items.reduce((sum,item)=>sum+Number(item.subtotal),0);return{carrinhoId:0,usuarioId:0,carrinhoItens:items,subtotal,descontos:0,total:subtotal}}
type CartContextValue = { cart: Cart | null; loading: boolean; error: string; count: number; total: number; refresh: () => Promise<void>; add: (productId: number, quantity?: number) => Promise<void>; update: (itemId: number, quantity: number) => Promise<void>; remove: (itemId: number) => Promise<void>; clear: () => Promise<void> }
const CartContext = createContext<CartContextValue | null>(null)

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth(); const [cart,setCart]=useState<Cart|null>(()=>guestCart(readGuest())); const [loading,setLoading]=useState(false); const [error,setError]=useState('')
  const refresh=useCallback(async()=>{if(!user){setCart(guestCart(readGuest()));setError('');return}if(user.tipoUsuario!=='CLIENTE'){setCart(null);return}setLoading(true);try{setCart(await api.cart(user.id));setError('')}catch(cause){setError(cause instanceof Error?cause.message:'Não foi possível carregar sua sacola.')}finally{setLoading(false)}},[user])
  useEffect(()=>{void (async()=>{if(user?.tipoUsuario==='CLIENTE'){const pending=readGuest();if(pending.length){setLoading(true);try{for(const item of pending)await api.addCart(user.id,item.produtoId,item.quantidade);saveGuest([])}catch(cause){setError(cause instanceof Error?cause.message:'Não foi possível transferir sua sacola.')}finally{setLoading(false)}}}await refresh()})()},[user,refresh])
  const run=async(action:(id:number)=>Promise<Cart>)=>{if(!user||user.tipoUsuario!=='CLIENTE')throw new Error('A sacola persistida está disponível somente para clientes.');setLoading(true);setError('');try{setCart(await action(user.id))}catch(cause){setError(cause instanceof Error?cause.message:'Não foi possível atualizar sua sacola.');throw cause}finally{setLoading(false)}}
  const add=async(productId:number,quantity=1)=>{if(user)return run(id=>api.addCart(id,productId,quantity));const product=await api.product(productId);const items=readGuest();const existing=items.find(item=>item.produtoId===productId);if(existing){existing.quantidade=Math.min(product.quantidadeEstoque,existing.quantidade+quantity);existing.subtotal=existing.quantidade*Number(existing.precoUnitario)}else items.push({itemId:-productId,produtoId:product.id,produtoNome:product.nome,produtoImagemUrl:product.imagemUrl,quantidade:quantity,precoUnitario:Number(product.precoFinal),subtotal:quantity*Number(product.precoFinal)});saveGuest(items);setCart(guestCart(items))}
  const update=async(itemId:number,quantity:number)=>{quantity=Math.max(1,quantity);if(user)return run(id=>api.updateCart(id,itemId,quantity));const items=readGuest();const item=items.find(value=>value.itemId===itemId);if(item){item.quantidade=quantity;item.subtotal=quantity*Number(item.precoUnitario)}saveGuest(items);setCart(guestCart(items))}
  const remove=async(itemId:number)=>{if(user)return run(id=>api.removeCart(id,itemId));const items=readGuest().filter(item=>item.itemId!==itemId);saveGuest(items);setCart(guestCart(items))}
  const clear=async()=>{if(user)return run(id=>api.clearCart(id));saveGuest([]);setCart(guestCart([]))}
  const value=useMemo<CartContextValue>(()=>({cart,loading,error,count:cart?.carrinhoItens.reduce((sum,item)=>sum+item.quantidade,0)||0,total:Number(cart?.total??0),refresh,add,update,remove,clear}),[cart,loading,error,refresh,user])
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
export const useCart=()=>{const context=useContext(CartContext);if(!context)throw new Error('CartProvider ausente');return context}
