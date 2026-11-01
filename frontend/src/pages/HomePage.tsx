import { ArrowRight, ChevronLeft, ChevronRight, Leaf, ShieldCheck, Sparkles } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { ProductCard } from '../components/ProductCard'
import { ErrorState, Loading } from '../components/States'
import { campaigns, goals } from '../data/demo'
import { api } from '../services/api'
import type { Product } from '../types/api'

const goalImages = [
  '/assets/imagens/objetivos-vita/emagrecer.png',
  '/assets/imagens/objetivos-vita/ganhar-massa.png',
  '/assets/imagens/objetivos-vita/performance.png',
  '/assets/imagens/objetivos-vita/bem-estar.png',
]

const functionLines = [
  ['Esporte & Fitness','/catalogo?objetivos=GANHO_DE_MASSA','/assets/imagens/musculacao'],
  ['Snacks Proteicos','/catalogo?busca=snack','/assets/imagens/corrida'],
  ['Alta Performance','/catalogo?objetivos=PERFORMANCE','/assets/imagens/ciclismo'],
  ['Corpo & Mente','/catalogo?objetivos=SAUDE_E_BEM_ESTAR','/assets/imagens/pilates.jpg'],
  ['Pele & Beleza','/catalogo?busca=colageno','/assets/imagens/banner-foto-1.jpg'],
]

export function HomePage() {
  const [products, setProducts] = useState<Product[]>([]); const [best,setBest]=useState<Product[]>([]); const [bestSellers,setBestSellers]=useState<Product[]>([]); const [error, setError] = useState(''); const [loading, setLoading] = useState(true); const [slide, setSlide] = useState(0); const [paused, setPaused] = useState(false)
  const touchStart = useRef<number | null>(null)
  const bestSellerTrack = useRef<HTMLDivElement | null>(null); const [sellerPaused,setSellerPaused]=useState(false)
  const moveBestSellers = (direction: number) => { const track=bestSellerTrack.current; const first=track?.firstElementChild as HTMLElement|null; if(!track||!first)return; const gap=Number.parseFloat(getComputedStyle(track).columnGap)||18; const step=first.offsetWidth+gap; const max=track.scrollWidth-track.clientWidth; const next=direction>0?(track.scrollLeft+step>=max-2?0:track.scrollLeft+step):(track.scrollLeft<=2?max:track.scrollLeft-step); track.scrollTo({left:next,behavior:'smooth'}) }
  const load = () => { setLoading(true); setError(''); Promise.all([api.products({ tamanho: 12, ordenacao: 'NOME_ASC' }),api.products({tamanho:4,destaque:true}),api.products({tamanho:8,maisVendidos:true})]).then(([page,featured,sellers]) => {setProducts(page.content);setBest(featured.content);setBestSellers(sellers.content)}).catch((e: Error) => setError(e.message)).finally(() => setLoading(false)) }
  useEffect(load, []); useEffect(() => { if (paused) return; const timer = setInterval(() => setSlide((v) => (v + 1) % campaigns.length), 6500); return () => clearInterval(timer) }, [paused])
  useEffect(() => { if(sellerPaused||bestSellers.length<2||window.matchMedia('(prefers-reduced-motion: reduce)').matches)return; const timer=setInterval(()=>moveBestSellers(1),3800); return()=>clearInterval(timer) },[sellerPaused,bestSellers.length])
  const moveSlide = (direction: number) => setSlide((current) => (current + direction + campaigns.length) % campaigns.length)
  const finishSwipe = (clientX: number) => { if (touchStart.current === null) return; const distance = clientX - touchStart.current; if (Math.abs(distance) > 45) moveSlide(distance < 0 ? 1 : -1); touchStart.current = null }
  const campaign = campaigns[slide]; const offers = products.filter((p) => Number(p.precoFinal) < Number(p.preco)).slice(0, 4); const offerProducts = offers.length ? offers : best.length ? best : products.slice(0, 4); const featuredProducts = best.length ? best : products.slice(4, 8)
  return <>
    <section className="hero hero-carousel" onPointerEnter={() => setPaused(true)} onPointerLeave={() => setPaused(false)} onFocus={() => setPaused(true)} onBlur={() => setPaused(false)} onTouchStart={(event) => { touchStart.current = event.touches[0].clientX }} onTouchEnd={(event) => finishSwipe(event.changedTouches[0].clientX)} aria-roledescription="carrossel" aria-label="Campanhas Vita Fortis"><div className="hero-backdrop" style={{ backgroundImage: `linear-gradient(90deg,rgba(8,35,25,.94),rgba(8,35,25,.35)),url('${campaign.image}')` }} /><div className="container hero-content"><span className="hero-kicker"><Sparkles size={16} /> {campaign.eyebrow}</span><h1>{campaign.title}</h1><p>{campaign.text}</p><Link className="button primary" to={campaign.to}>Conhecer seleção <ArrowRight size={18} /></Link></div><button className="hero-arrow left" onClick={() => moveSlide(-1)} aria-label="Banner anterior"><ChevronLeft /></button><button className="hero-arrow right" onClick={() => moveSlide(1)} aria-label="Próximo banner"><ChevronRight /></button><div className="hero-dots">{campaigns.map((item,i) => <button key={item.title} className={i === slide ? 'active' : ''} onClick={() => setSlide(i)} aria-label={`Exibir banner ${i + 1}`} aria-current={i === slide ? 'true' : undefined} />)}</div></section>
    <section className="section container intent-section"><div className="section-heading"><div><span className="eyebrow">Comece por você</span><h2>EU QUERO...</h2><p>Escolha seu objetivo e encontre produtos relacionados.</p></div></div><div className="goal-grid">{goals.map(([value,label],i) => <Link className="goal-card" key={value} to={`/catalogo?objetivos=${value}`}><img src={goalImages[i]} alt="" width="900" height="1100" loading="lazy" decoding="async" /><span className="goal-card-shade" aria-hidden="true" /><span className="goal-card-content"><small>0{i + 1}</small><strong>{label}</strong><span>Ver produtos <ArrowRight /></span></span></Link>)}</div></section>
    <section className="section container home-product-section"><div className="section-heading"><div><span className="eyebrow">Condições especiais</span><h2>Ofertas para aproveitar agora</h2></div><Link className="text-link" to="/ofertas">Ver ofertas <ArrowRight size={16} /></Link></div>{loading ? <Loading label="Buscando produtos…" /> : error ? <ErrorState message={error} retry={load} /> : <div className="product-grid">{offerProducts.map((p) => <ProductCard key={p.id} product={p} />)}</div>}</section>
    <section className="section best-sellers-section"><div className="container"><div className="section-heading"><div><span className="eyebrow">Favoritos dos clientes</span><h2>Mais vendidos</h2><p>Os produtos mais escolhidos na Vita Fortis.</p></div><Link className="text-link" to="/catalogo?maisVendidos=true">Ver todos <ArrowRight size={16} /></Link></div>{loading ? <Loading label="Buscando os mais vendidos…" /> : error ? <ErrorState message={error} retry={load} /> : <div className="best-sellers-carousel" onPointerEnter={()=>setSellerPaused(true)} onPointerLeave={()=>setSellerPaused(false)} onFocus={()=>setSellerPaused(true)} onBlur={()=>setSellerPaused(false)}><button className="best-sellers-arrow previous" onClick={()=>moveBestSellers(-1)} aria-label="Ver produtos anteriores"><ChevronLeft /></button><div className="best-sellers-track" ref={bestSellerTrack}>{bestSellers.map((product,index) => <div className="best-seller-item" key={product.id}><span className="best-seller-rank">{String(index + 1).padStart(2,'0')}</span><ProductCard product={product} /></div>)}</div><button className="best-sellers-arrow next" onClick={()=>moveBestSellers(1)} aria-label="Ver próximos produtos"><ChevronRight /></button></div>}</div></section>
    <section className="section container home-product-section"><div className="section-heading"><div><span className="eyebrow">Seleção Vita Fortis</span><h2>Produtos em destaque</h2></div></div>{featuredProducts.length?<div className="product-grid">{featuredProducts.map((p) => <ProductCard key={p.id} product={p} />)}</div>:!loading&&!error&&<p className="empty-inline">Os destaques aparecerão quando houver produtos ativos no catálogo.</p>}</section>
    <section className="function-section"><div className="container"><div className="function-heading"><span className="eyebrow light">Escolha pela sua rotina</span><h2>Uma Linha, Uma Função</h2></div><div className="function-grid">{functionLines.map(([label,to,image]) => <Link key={label} to={to} className="function-card"><img src={image} onError={(e) => { e.currentTarget.src = '/assets/imagens/banner-foto-1.jpg' }} alt="" width="520" height="720" loading="lazy" decoding="async" /><span><strong>{label}</strong><i aria-hidden="true"><ArrowRight /></i></span></Link>)}</div></div></section>
    <section className="section container home-shortcuts-section"><div className="shortcut-grid"><Link to="/novidades"><Sparkles /><strong>Novidades</strong><span>Conheça os lançamentos</span></Link><Link to="/kits"><Leaf /><strong>Kits</strong><span>Combinações para sua rotina</span></Link><Link to="/ofertas"><ArrowRight /><strong>Ofertas</strong><span>Condições especiais</span></Link><Link to="/marcas"><ShieldCheck /><strong>Marcas</strong><span>Encontre suas preferidas</span></Link></div></section>
  </>
}
