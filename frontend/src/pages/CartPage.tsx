import { ArrowRight, Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react'
import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { useCart } from '../app/CartContext'
import { Empty, Loading } from '../components/States'
import { api } from '../services/api'
import type { Address, AddressInput } from '../types/api'
import { money } from '../utils/format'

const emptyAddress: AddressInput = {
  apelido: 'Casa', cep: '', logradouro: '', numero: '', complemento: '', bairro: '',
  cidade: '', uf: '', principal: true,
}

export function CartPage() {
  const { user } = useAuth()
  const { cart, loading, total, update, remove, clear, refresh } = useCart()
  const [coupon, setCoupon] = useState('')
  const [message, setMessage] = useState('')
  const [ordering, setOrdering] = useState(false)
  const [reviewing,setReviewing] = useState(false)
  const [receiving, setReceiving] = useState<'RETIRADA' | 'ENTREGA'>('RETIRADA')
  const [payment, setPayment] = useState('PIX')
  const [addresses, setAddresses] = useState<Address[]>([])
  const [addressId, setAddressId] = useState<number | undefined>()
  const [newAddress, setNewAddress] = useState<AddressInput>(emptyAddress)
  const navigate = useNavigate()

  useEffect(() => {
    if (!user) return
    api.addresses(user.id).then((items) => {
      setAddresses(items)
      setAddressId(items.find((item) => item.principal)?.id ?? items[0]?.id)
    }).catch(() => undefined)
  }, [user])

  if (!user) return <div className="page container"><Empty title="Sua sacola espera por você" text="Entre na sua conta para carregar e manter seus produtos." /><div className="center"><Link className="button primary" to="/entrar">Entrar na conta</Link></div></div>
  if (loading && !cart) return <div className="page container"><Loading /></div>
  if (!cart?.carrinhoItens.length) return <div className="page container"><Empty title="Sua sacola está vazia" text="Explore o catálogo para encontrar produtos para seu objetivo." /><div className="center"><Link className="button primary" to="/catalogo">Explorar produtos</Link></div></div>

  const finish = async (event: FormEvent) => {
    event.preventDefault()
    if (!reviewing) { setReviewing(true); setMessage('Revise todos os dados antes de confirmar o pedido.'); return }
    setOrdering(true)
    setMessage('')
    try {
      let selectedAddressId = addressId
      if (receiving === 'ENTREGA' && !selectedAddressId) {
        const address = await api.createAddress(user.id, newAddress)
        selectedAddressId = address.id
      }
      const order = await api.createOrder({
        usuarioId: user.id,
        itens: cart.carrinhoItens.map((item) => ({ produtoId: item.produtoId, quantidade: item.quantidade })),
        cupomId: cart.cupomId,
        formaRecebimento: receiving,
        enderecoId: receiving === 'ENTREGA' ? selectedAddressId : undefined,
        formaPagamento: payment,
      })
      await clear()
      navigate(`/pedidos?novo=${order.id}`)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Não foi possível criar o pedido.')
    } finally {
      setOrdering(false)
    }
  }

  const applyCoupon = async () => {
    try {
      await api.applyCoupon(user.id, coupon.trim())
      await refresh()
      setMessage('Cupom aplicado.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Cupom inválido.')
    }
  }

  const field = (key: keyof AddressInput, value: string) =>
    setNewAddress((current) => ({ ...current, [key]: value }))

  return <div className="page container">
    <header className="page-header compact"><span className="eyebrow">Sua seleção</span><h1>Sacola e checkout</h1></header>
    <form className="cart-layout" onSubmit={finish}>
      <section className="cart-items">
        {cart.carrinhoItens.map((item) => <article className="cart-item" key={item.itemId}>
          <div className="cart-thumb"><ShoppingBag /></div>
          <div className="cart-item-main"><h2>{item.produtoNome}</h2><p>{money(item.precoUnitario)} por unidade</p><div className="quantity small"><button type="button" onClick={() => item.quantidade === 1 ? remove(item.itemId) : update(item.itemId, item.quantidade - 1)} aria-label="Diminuir"><Minus /></button><span>{item.quantidade}</span><button type="button" onClick={() => update(item.itemId, item.quantidade + 1)} aria-label="Aumentar"><Plus /></button></div></div>
          <div className="cart-item-price"><strong>{money(item.subtotal)}</strong><button type="button" onClick={() => remove(item.itemId)} aria-label="Remover"><Trash2 /></button></div>
        </article>)}
        <button type="button" className="text-button danger" onClick={clear}><Trash2 size={16} /> Limpar sacola</button>
      </section>
      <aside className="summary">
        <h2>Finalização</h2>
        {reviewing&&<div className="review-alert"><strong>Revisão final</strong><span>Confira itens, entrega, pagamento, desconto e total. O pagamento ficará pendente para confirmação administrativa.</span></div>}
        <label>Como deseja receber?<select disabled={reviewing} value={receiving} onChange={(event) => setReceiving(event.target.value as 'RETIRADA' | 'ENTREGA')}><option value="RETIRADA">Retirar na loja</option><option value="ENTREGA">Receber no endereço</option></select></label>
        {receiving === 'ENTREGA' && (addresses.length
          ? <label>Endereço<select value={addressId} onChange={(event) => setAddressId(Number(event.target.value))}>{addresses.map((address) => <option key={address.id} value={address.id}>{address.apelido || address.logradouro} - {address.cep}</option>)}</select></label>
          : <div className="form-grid"><label>CEP<input required pattern="[0-9]{8}" value={newAddress.cep} onChange={(event) => field('cep', event.target.value.replace(/\D/g, ''))} /></label><label>Logradouro<input required value={newAddress.logradouro} onChange={(event) => field('logradouro', event.target.value)} /></label><label>Número<input required value={newAddress.numero} onChange={(event) => field('numero', event.target.value)} /></label><label>Complemento<input value={newAddress.complemento} onChange={(event) => field('complemento', event.target.value)} /></label><label>Bairro<input required value={newAddress.bairro} onChange={(event) => field('bairro', event.target.value)} /></label><label>Cidade<input required value={newAddress.cidade} onChange={(event) => field('cidade', event.target.value)} /></label><label>UF<input required maxLength={2} value={newAddress.uf} onChange={(event) => field('uf', event.target.value.toUpperCase())} /></label></div>)}
        <label>Pagamento<select disabled={reviewing} value={payment} onChange={(event) => setPayment(event.target.value)}><option value="PIX">Pix simulado</option><option value="CARTAO">Cartão simulado</option><option value="BOLETO">Boleto simulado</option></select></label>
        <div><span>Subtotal</span><strong>{money(cart.subtotal ?? total)}</strong></div>
        {(cart.descontos||0)>0&&<div><span>Desconto ({cart.cupomCodigo})</span><strong>-{money(cart.descontos)}</strong></div>}
        <div><span>Frete</span><span>{receiving === 'RETIRADA' ? 'Grátis' : 'Calculado pelo servidor'}</span></div>
        <div className="coupon"><label>Cupom<input value={coupon} onChange={(event) => setCoupon(event.target.value)} placeholder="Digite o código" /></label><button type="button" onClick={applyCoupon}>Aplicar</button></div>
        {(cart.cashbackColaborador||0)>0&&<p className="cashback-disclosure">Este pedido gerará {money(cart.cashbackColaborador)} de cashback para {cart.colaboradorNome}. Esse valor não é desconto nem crédito para o cliente.</p>}
        <div className="summary-total"><span>Total</span><strong>{money(cart.total ?? total)}</strong></div>
        {message && <p className="summary-message">{message}</p>}
        {reviewing&&<button type="button" className="button secondary" onClick={()=>{setReviewing(false);setMessage('')}}>Voltar e editar</button>}
        <button className="button primary" disabled={ordering}>{ordering ? 'Finalizando…' : reviewing ? <>Confirmar pedido <ArrowRight size={18}/></> : <>Revisar pedido <ArrowRight size={18} /></>}</button>
      </aside>
    </form>
  </div>
}
