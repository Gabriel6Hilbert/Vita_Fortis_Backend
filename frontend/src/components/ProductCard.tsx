import { ArrowRight, Check, Heart, ShoppingBag } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import type { Product } from '../types/api'
import { money, titleCase } from '../utils/format'
import { useAuth } from '../app/AuthContext'
import { useCart } from '../app/CartContext'
import { api } from '../services/api'
import { favoriteIds, toggleFavorite } from '../utils/commerce'

const fallback = '/assets/imagens/proteina_exemplo.png'

export function ProductCard({ product }: { product: Product }) {
  const { user } = useAuth(); const { add } = useCart(); const navigate = useNavigate(); const [busy, setBusy] = useState(false); const [favorite, setFavorite] = useState(() => favoriteIds().includes(product.id)); const [added, setAdded] = useState(false)
  useEffect(() => { if (user) api.favorites(user.id).then((items) => setFavorite(items.some((item) => item.id === product.id))).catch(() => undefined) },[user,product.id])
  const discounted = Number(product.precoFinal) < Number(product.preco)
  const handleAdd = async () => { if (!user) { navigate('/entrar', { state: { from: `/produto/${product.id}` } }); return }; setBusy(true); try { await add(product.id); setAdded(true); setTimeout(() => setAdded(false), 1800) } catch (error) { alert(error instanceof Error ? error.message : 'Não foi possível adicionar.') } finally { setBusy(false) } }
  return <article className="product-card">
    <Link to={`/produto/${product.id}`} className="product-image-wrap">
      {discounted && <span className="discount-badge">-{Math.round(Number(product.descontoPercentual || 0))}%</span>}
      <button className={`favorite-button ${favorite ? 'active' : ''}`} onClick={(e) => { e.preventDefault(); if (!user) { setFavorite(toggleFavorite(product.id)); return } const next=!favorite; setFavorite(next); void (next ? api.addFavorite(user.id,product.id) : api.removeFavorite(user.id,product.id)).catch(() => setFavorite(!next)) }} aria-label={favorite ? `Remover ${product.nome} dos favoritos` : `Favoritar ${product.nome}`}><Heart fill={favorite ? 'currentColor' : 'none'} /></button>
      <img src={product.imagemUrl || fallback} onError={(event) => { event.currentTarget.src = fallback }} alt={product.nome} className="product-image" />
    </Link>
    <div className="product-body">
      <span className="eyebrow">{product.marca || titleCase(product.categoria)}</span>
      <Link to={`/produto/${product.id}`}><h3>{product.nome}</h3></Link>
      <div className="availability"><Check size={14} /> {product.quantidadeEstoque > 0 ? `${product.quantidadeEstoque} em estoque` : 'Indisponível'}</div>
      <div className="price-row">
        <div>{discounted && <small>{money(product.preco)}</small>}<strong>{money(product.precoFinal)}</strong></div>
        <button className="icon-button filled" onClick={handleAdd} disabled={busy || product.quantidadeEstoque <= 0} aria-label={`Adicionar ${product.nome} à sacola`}>{added ? <Check size={19} /> : <ShoppingBag size={19} />}</button>
      </div>
      <Link className="text-link" to={`/produto/${product.id}`}>Ver detalhes <ArrowRight size={15} /></Link>
    </div>
  </article>
}
