import { ArrowRight, ChevronDown, Menu, MessageCircle, Search, ShoppingBag, UserRound, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../services/api'
import { isAdmin, isCollaborator, type CategorySummary, type GoalSummary, type Product, type StoreInfo } from '../types/api'
import { useAuth } from '../app/AuthContext'
import { useCart } from '../app/CartContext'

export function Layout() {
  const [menu, setMenu] = useState(false); const [megaMenu,setMegaMenu]=useState(false); const [search, setSearch] = useState(''); const [store, setStore] = useState<StoreInfo | null>(null); const [megaCategories,setMegaCategories]=useState<CategorySummary[]>([]); const [megaGoals,setMegaGoals]=useState<GoalSummary[]>([]); const [megaBest,setMegaBest]=useState<Product[]>([])
  const { user, logout } = useAuth(); const { count } = useCart(); const navigate = useNavigate(); const location = useLocation()
  useEffect(() => { api.store().then(setStore).catch(() => undefined) }, [])
  useEffect(() => {
    if (!megaMenu) return
    let current = true
    void api.categories().then(value => { if (current) setMegaCategories(value) }).catch(() => undefined)
    void api.goals().then(value => { if (current) setMegaGoals(value) }).catch(() => undefined)
    void api.products({ maisVendidos: true, tamanho: 8 }).then(value => {
      if (current) setMegaBest(value.content.slice(0, 7))
    }).catch(() => undefined)
    return () => { current = false }
  }, [megaMenu])
  useEffect(() => { if (!location.pathname.startsWith('/catalogo')) setSearch(''); setMegaMenu(false); setMenu(false) }, [location.pathname,location.search])
  const closeNavigation=()=>{setMegaMenu(false);setMenu(false)}
  const goHome = () => { setSearch(''); closeNavigation() }
  const submit = (event: React.FormEvent) => { event.preventDefault(); const term=search.trim(); navigate(term ? `/catalogo?busca=${encodeURIComponent(term)}` : '/catalogo'); setMenu(false) }
  return <div className="app-shell">
    <a className="skip-link" href="#main">Pular para o conteúdo</a>
    <div className="announcement" aria-label="Informações da loja"><div className="announcement-track">{[0,1].map(group=><div className="announcement-group" key={group} aria-hidden={group === 1 ? true : undefined}>{[0,1].map(item=><span key={item} aria-hidden={item === 1 ? true : undefined}>COMPRA SEGURA <b>•</b> ATENDIMENTO PERSONALIZADO <b>•</b> ENTREGA ACOMPANHADA <b>•</b></span>)}</div>)}</div></div>
    <header className="header">
      <div className="header-main container">
        <button className="menu-toggle" onClick={() => setMenu(!menu)} aria-label={menu ? 'Fechar menu' : 'Abrir menu'} aria-expanded={menu} aria-controls="main-navigation">{menu ? <X /> : <Menu />}</button>
        <Link to="/" className="brand" onClick={goHome} aria-label="Vita Fortis Suplementos — início"><span className="brand-mark" aria-hidden="true"><img src="/assets/imagens/logo_vitafortisBarraPesquisaSemFundo.png" alt="" width="437" height="465" /></span><span className="brand-copy"><strong>Vita Fortis</strong><small>Suplementos</small></span></Link>
        {location.pathname.replace(/\/+$/, '').toLowerCase() !== '/catalogo' && <form className="search" onSubmit={submit}><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Busque suplemento, marca ou objetivo" aria-label="Buscar produtos" /><button type="submit" aria-label="Pesquisar"><Search size={18} /></button></form>}
        <div className="header-actions">
          <Link to={user ? '/conta' : '/entrar'} className="header-action"><UserRound /><span>{user ? user.nome.split(' ')[0] : 'Entrar'}</span></Link>
          {(!user || user.tipoUsuario !== 'ADMIN') && <Link to="/sacola" className="header-action bag"><ShoppingBag /><span>Sacola</span>{count > 0 && <b>{count}</b>}</Link>}
        </div>
      </div>
      <nav id="main-navigation" className={`nav ${menu ? 'open' : ''}`} aria-label="Navegação principal" onMouseLeave={()=>setMegaMenu(false)} onKeyDown={(event)=>{if(event.key==='Escape')setMegaMenu(false)}}><div className="container">
        <NavLink to="/" end onClick={goHome}>Home</NavLink><button className={`nav-mega-trigger ${megaMenu?'active':''}`} onClick={()=>setMegaMenu(value=>!value)} onMouseEnter={()=>{if(window.matchMedia('(hover: hover)').matches)setMegaMenu(true)}} aria-expanded={megaMenu} aria-controls="catalog-mega-menu">Todos os produtos <ChevronDown /></button><NavLink to="/ofertas" onClick={closeNavigation}>Ofertas</NavLink><NavLink to="/marcas" onClick={closeNavigation}>Marcas</NavLink><NavLink to="/kits" onClick={closeNavigation}>Kits</NavLink><NavLink to="/objetivos" onClick={closeNavigation}>Objetivos</NavLink>{user?.tipoUsuario==='CLIENTE'&&<NavLink to="/favoritos" onClick={closeNavigation}>Meus favoritos</NavLink>}{user&&user.tipoUsuario!=='ADMIN'&&<NavLink to="/pedidos" onClick={closeNavigation}>Meus pedidos</NavLink>}{isAdmin(user) && <NavLink to="/admin" onClick={closeNavigation}>Painel admin</NavLink>}{isCollaborator(user)&&<NavLink to="/colaborador" onClick={closeNavigation}>Meu cashback</NavLink>}
        {user && <button className="nav-logout" onClick={() => { logout(); navigate('/') }}>Sair</button>}
      </div>{megaMenu&&<div className="catalog-mega" id="catalog-mega-menu"><div className="container catalog-mega-grid"><section><h2>Produtos por categoria</h2>{megaCategories.slice(0,10).map(item=><Link key={item.categoria} to={`/catalogo?categoria=${encodeURIComponent(item.categoria)}`} onClick={closeNavigation}>{item.nome||item.categoria}<small>{item.quantidadeProdutos}</small></Link>)}<Link className="mega-see-all" to="/catalogo" onClick={closeNavigation}>Ver todos <ArrowRight /></Link></section><section><h2>Objetivos</h2>{megaGoals.slice(0,10).map(item=><Link key={item.codigo} to={`/catalogo?objetivos=${encodeURIComponent(item.codigo)}`} onClick={closeNavigation}>{item.nome}<small>{item.quantidadeProdutos}</small></Link>)}<Link className="mega-see-all" to="/objetivos" onClick={closeNavigation}>Ver todos <ArrowRight /></Link></section><section><h2>Mais vendidos</h2>{megaBest.map(product=><Link key={product.id} to={`/produto/${product.id}`} onClick={closeNavigation}>{product.nome}</Link>)}<Link className="mega-see-all" to="/catalogo?maisVendidos=true" onClick={closeNavigation}>Ver todos <ArrowRight /></Link></section><section><h2>Linhas e funções</h2><Link to="/catalogo?objetivos=GANHO_DE_MASSA" onClick={closeNavigation}>Esporte &amp; Fitness</Link><Link to="/catalogo?busca=snack" onClick={closeNavigation}>Snacks Proteicos</Link><Link to="/catalogo?objetivos=PERFORMANCE" onClick={closeNavigation}>Alta Performance</Link><Link to="/catalogo?objetivos=SAUDE_E_BEM_ESTAR" onClick={closeNavigation}>Corpo &amp; Mente</Link><Link to="/catalogo?busca=colageno" onClick={closeNavigation}>Pele &amp; Beleza</Link><Link className="mega-see-all" to="/catalogo" onClick={closeNavigation}>Explorar catálogo <ArrowRight /></Link></section></div></div>}</nav>
    </header>
    <main id="main"><Outlet /></main>
    {store?.telefone && <a className="whatsapp-float" href={`https://wa.me/55${store.telefone.replace(/\D/g,'').replace(/^55/,'')}`} target="_blank" rel="noreferrer" aria-label="Falar com a Vita Fortis pelo WhatsApp"><MessageCircle aria-hidden="true" /><span>WhatsApp</span></a>}
    <footer className="footer"><div className="container footer-grid"><div><img src="/assets/imagens/logo_vitafortisBarraPesquisaSemFundo.png" alt="Vita Fortis Suplementos" width="190" height="64" /><p>{store?.descricao || 'Suplementos para sua evolução, com escolhas mais simples e conscientes.'}</p></div><div><h3>Vita Fortis</h3><Link to="/catalogo">Catálogo</Link><Link to="/sobre">Sobre nós</Link><Link to="/favoritos">Favoritos</Link></div><div><h3>Ajuda e políticas</h3><Link to="/contato">Fale conosco</Link><Link to="/faq">Perguntas frequentes</Link><Link to="/politica-de-privacidade">Privacidade</Link><Link to="/politica-de-cookies">Cookies</Link><Link to="/termos-de-uso-e-compra">Termos de compra</Link><Link to="/entrega-e-retirada">Entrega e retirada</Link><Link to="/trocas-devolucoes-e-arrependimento">Trocas e devoluções</Link></div><div><h3>Atendimento</h3><p>{store?.telefone || 'Telefone em atualização'}</p><p>{store?.email || 'E-mail em atualização'}</p><p>Olaria</p></div></div><div className="container copyright">© {new Date().getFullYear()} Vita Fortis.</div></footer>
  </div>
}
