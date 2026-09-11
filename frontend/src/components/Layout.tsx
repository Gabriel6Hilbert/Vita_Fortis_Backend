import { Menu, Search, ShoppingBag, UserRound, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../services/api'
import { isAdmin, isCollaborator, type StoreInfo } from '../types/api'
import { useAuth } from '../app/AuthContext'
import { useCart } from '../app/CartContext'

export function Layout() {
  const [menu, setMenu] = useState(false); const [search, setSearch] = useState(''); const [store, setStore] = useState<StoreInfo | null>(null)
  const { user, logout } = useAuth(); const { count } = useCart(); const navigate = useNavigate(); const location = useLocation()
  useEffect(() => { api.store().then(setStore).catch(() => undefined) }, [])
  useEffect(() => { if (!location.pathname.startsWith('/catalogo')) setSearch('') }, [location.pathname])
  const goHome = () => { setSearch(''); setMenu(false) }
  const submit = (event: React.FormEvent) => { event.preventDefault(); const term=search.trim(); navigate(term ? `/catalogo?busca=${encodeURIComponent(term)}` : '/catalogo'); setMenu(false) }
  const announcementItems = ['Compra segura', 'Atendimento personalizado Vita Fortis', 'Retirada ou entrega', 'Acompanhe seus pedidos online']
  return <div className="app-shell">
    <a className="skip-link" href="#main">Pular para o conteúdo</a>
    <div className="announcement" aria-label="Informações da loja"><div className="announcement-track">{[0, 1].map((copy) => <div className="announcement-group" aria-hidden={copy === 1} key={copy}>{announcementItems.map((item) => <span key={item}>{item}<b>•</b></span>)}</div>)}</div></div>
    <header className="header">
      <div className="header-main container">
        <button className="menu-toggle" onClick={() => setMenu(!menu)} aria-label="Abrir menu">{menu ? <X /> : <Menu />}</button>
        <Link to="/" className="brand" onClick={goHome}><img src="/assets/imagens/logo_vitafortisBarraPesquisaSemFundo.png" alt="Vita Fortis Suplementos" /></Link>
        <form className="search" onSubmit={submit}><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Busque suplemento, marca ou objetivo" aria-label="Buscar produtos" /><button type="submit" aria-label="Pesquisar"><Search size={18} /></button></form>
        <div className="header-actions">
          <Link to={user ? '/conta' : '/entrar'} className="header-action"><UserRound /><span>{user ? user.nome.split(' ')[0] : 'Entrar'}</span></Link>
          {(!user || user.tipoUsuario === 'CLIENTE') && <Link to="/sacola" className="header-action bag"><ShoppingBag /><span>Sacola</span>{count > 0 && <b>{count}</b>}</Link>}
        </div>
      </div>
      <nav className={`nav ${menu ? 'open' : ''}`} aria-label="Navegação principal"><div className="container">
        <NavLink to="/" end onClick={goHome}>Home</NavLink><NavLink to="/catalogo" onClick={() => {setSearch('');setMenu(false)}}>Todos os produtos</NavLink><NavLink to="/ofertas" onClick={() => setMenu(false)}>Ofertas</NavLink><NavLink to="/marcas" onClick={() => setMenu(false)}>Marcas</NavLink><NavLink to="/kits" onClick={() => setMenu(false)}>Kits</NavLink><NavLink to="/objetivos" onClick={() => setMenu(false)}>Objetivos</NavLink>{user?.tipoUsuario==='CLIENTE'&&<NavLink to="/favoritos" onClick={() => setMenu(false)}>Meus favoritos</NavLink>}{user?.tipoUsuario==='CLIENTE'&&<NavLink to="/pedidos" onClick={() => setMenu(false)}>Meus pedidos</NavLink>}{isAdmin(user) && <NavLink to="/admin" onClick={() => setMenu(false)}>Painel admin</NavLink>}{isCollaborator(user)&&<NavLink to="/colaborador" onClick={()=>setMenu(false)}>Meu cashback</NavLink>}
        {user && <button className="nav-logout" onClick={() => { logout(); navigate('/') }}>Sair</button>}
      </div></nav>
    </header>
    <main id="main"><Outlet /></main>
    {store?.telefone && <a className="whatsapp-float" href={`https://wa.me/55${store.telefone.replace(/\D/g,'').replace(/^55/,'')}`} target="_blank" rel="noreferrer" aria-label="Falar com a Vita Fortis pelo WhatsApp">WhatsApp</a>}
    <footer className="footer"><div className="container footer-grid"><div><img src="/assets/imagens/logo-vita-fortis-brand.webp" alt="Vita Fortis Suplementos" /><p>{store?.descricao || 'Suplementos para sua evolução, com escolhas mais simples e conscientes.'}</p></div><div><h3>Vita Fortis</h3><Link to="/catalogo">Catálogo</Link><Link to="/sobre">Sobre nós</Link><Link to="/favoritos">Favoritos</Link></div><div><h3>Ajuda</h3><Link to="/contato">Fale conosco</Link><Link to="/faq">Perguntas frequentes</Link><Link to="/politicas">Políticas</Link><Link to="/trabalhe-conosco">Trabalhe conosco</Link></div><div><h3>Atendimento</h3><p>{store?.telefone || 'Telefone em atualização'}</p><p>{store?.email || 'E-mail em atualização'}</p><p>{store?.endereco || 'Endereço em atualização'}</p></div></div><div className="container copyright">© {new Date().getFullYear()} Vita Fortis.</div></footer>
  </div>
}
