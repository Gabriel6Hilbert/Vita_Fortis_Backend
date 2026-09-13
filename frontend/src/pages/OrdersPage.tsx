import { CheckCircle2, Clock3, Eye, LoaderCircle, X } from 'lucide-react'
import { useEffect, useRef, useState, type SyntheticEvent } from 'react'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { Empty, ErrorState, Loading } from '../components/States'
import { api } from '../services/api'
import type { Order, OrderDetail } from '../types/api'
import { date, money, titleCase } from '../utils/format'
import '../styles/commerce-feedback.css'

const fallback = '/assets/imagens/logo-vita-fortis-brand.webp'
const fallbackImage = (event: SyntheticEvent<HTMLImageElement>) => {
  const image = event.currentTarget
  if (!image.src.endsWith(fallback)) image.src = fallback
}

export function OrdersPage() {
  const { user } = useAuth()
  const [orders, setOrders] = useState<Order[]>([])
  const [detail, setDetail] = useState<OrderDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [detailError, setDetailError] = useState('')
  const [openingOrderId, setOpeningOrderId] = useState<number | null>(null)
  const [params] = useSearchParams()
  const requestVersion = useRef(0)
  const detailTrigger = useRef<HTMLButtonElement | null>(null)
  const closeButton = useRef<HTMLButtonElement | null>(null)

  const load = () => {
    if (!user) return
    const version = ++requestVersion.current
    setLoading(true)
    setError('')
    setDetailError('')
    api.orders(user.id)
      .then(result => { if (version === requestVersion.current) setOrders(result) })
      .catch((e: Error) => { if (version === requestVersion.current) setError(e.message) })
      .finally(() => { if (version === requestVersion.current) setLoading(false) })
  }
  useEffect(() => {
    setDetail(null)
    setOpeningOrderId(null)
    load()
    return () => { requestVersion.current += 1 }
  }, [user])

  useEffect(() => {
    if (!detail) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    closeButton.current?.focus()
    return () => {
      document.body.style.overflow = previousOverflow
      detailTrigger.current?.focus()
    }
  }, [detail])

  const open = async (id: number, trigger: HTMLButtonElement) => {
    if (openingOrderId !== null) return
    const version = requestVersion.current
    detailTrigger.current = trigger
    setOpeningOrderId(id)
    setDetailError('')
    try {
      const result = await api.order(id)
      if (version === requestVersion.current) setDetail(result)
    } catch (e) {
      if (version === requestVersion.current) setDetailError(e instanceof Error ? e.message : 'Não foi possível abrir o pedido. Tente novamente em Ver detalhes.')
    } finally {
      if (version === requestVersion.current) setOpeningOrderId(null)
    }
  }

  if (!user) return <Navigate to="/entrar" replace />

  const products = (order: Order) => <div className="order-items">{order.itens.map(item =>
    <div className="order-product" key={item.id}>
      <img src={item.produtoImagemUrl || fallback} onError={fallbackImage} alt="" loading="lazy" />
      <span><b>{item.produtoNome}</b><small>{item.quantidade} unidade(s) · {money(item.precoUnitario)} cada</small></span>
      <strong>{money(item.subtotal)}</strong>
    </div>,
  )}</div>

  return <div className="page container">
    <header className="page-header compact"><span className="eyebrow">Histórico de compras</span><h1>Meus pedidos</h1><p>Acompanhe os produtos, o pagamento, a forma de recebimento e cada atualização da compra.</p></header>
    {params.get('novo') && <div className="success-banner" role="status"><CheckCircle2 /><div><strong>Pedido #{params.get('novo')} criado!</strong><span>O pedido aguarda a confirmação administrativa do pagamento simulado.</span></div></div>}
    {detailError && <p className="commerce-feedback error" role="alert">{detailError}</p>}
    {loading ? <Loading /> : error ? <ErrorState message={error} retry={load} /> : !orders.length ? <><Empty title="Nenhum pedido ainda" text="Quando você finalizar sua primeira compra, ela aparecerá aqui." /><div className="center"><Link className="button primary" to="/catalogo">Ir ao catálogo</Link></div></> :
      <div className="order-list">{orders.map(order => <article className="order-card" key={order.id}>
        <header><div><span>Pedido #{order.id}</span><small>Realizado em {date(order.dataPedido)}</small></div><b className={`status ${order.status.toLowerCase()}`}><Clock3 /> {titleCase(order.status)}</b></header>
        {order.statusPagamento === 'PENDENTE' && <div className="payment-pending"><Clock3 /><div><strong>Pagamento aguardando confirmação</strong><span>{titleCase(order.formaPagamento || 'simulado')} {order.referenciaPagamentoMascarada && `• Referência ${order.referenciaPagamentoMascarada}`}</span></div></div>}
        {products(order)}
        <footer><span>{titleCase(order.formaRecebimento)} • Pagamento {titleCase(order.statusPagamento)}</span><strong>{money(order.total)}</strong>
          <button className="icon-button" disabled={openingOrderId !== null} aria-label={`Ver detalhes do pedido #${order.id}`} aria-busy={openingOrderId === order.id} onClick={event => void open(order.id, event.currentTarget)}>
            {openingOrderId === order.id ? <><LoaderCircle className="spin" aria-hidden="true" /> Abrindo…</> : <><Eye aria-hidden="true" /> Ver detalhes</>}
          </button>
        </footer>
      </article>)}</div>}
    {detail && <div className="modal-backdrop" onClick={event => { if (event.target === event.currentTarget) setDetail(null) }}>
      <div className="admin-modal small order-detail" role="dialog" aria-modal="true" aria-labelledby="order-detail-title" onKeyDown={event => {
        if (event.key === 'Escape') { event.preventDefault(); setDetail(null) }
        if (event.key === 'Tab') { event.preventDefault(); closeButton.current?.focus() }
      }}>
        <header><div><span className="eyebrow">Acompanhamento</span><h2 id="order-detail-title">Pedido #{detail.id}</h2></div><button ref={closeButton} aria-label="Fechar detalhes do pedido" onClick={() => setDetail(null)}><X /></button></header>
        <div className="order-detail-products">{detail.itens.map(item => <article key={item.id}><img src={item.produtoImagemUrl || fallback} onError={fallbackImage} alt="" /><div><strong>{item.produtoNome}</strong><small>{item.quantidade} × {money(item.precoUnitario)}</small></div><b>{money(item.subtotal)}</b></article>)}</div>
        <div className="financial-summary"><p><span>Subtotal</span><b>{money(detail.subtotal)}</b></p><p><span>Desconto {detail.cupomCodigo && `(${detail.cupomCodigo})`}</span><b>-{money(detail.desconto)}</b></p><p><span>Frete</span><b>{money(detail.frete)}</b></p><p><strong>Total</strong><strong>{money(detail.total)}</strong></p></div>
        <p><b>Recebimento:</b> {titleCase(detail.formaRecebimento)} {detail.enderecoMascarado && `— ${detail.enderecoMascarado}`}</p>
        <p><b>Pagamento:</b> {titleCase(detail.formaPagamento || '')} · {titleCase(detail.statusPagamento)} {detail.referenciaPagamentoMascarada && `· ${detail.referenciaPagamentoMascarada}`}</p>
        <h3>Linha do tempo</h3>
        {detail.historicoStatus?.length ? <div className="order-timeline">{detail.historicoStatus.map((h, i) => <article key={`${h.status}-${i}`}><i /><div><strong>{titleCase(h.status)}</strong><small>{date(h.data)}</small><p>{h.observacao || h.descricao}</p></div></article>)}</div> : <p className="empty-inline">As próximas atualizações deste pedido aparecerão aqui.</p>}
      </div>
    </div>}
  </div>
}