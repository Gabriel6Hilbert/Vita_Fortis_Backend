import { Menu, Search, ShoppingBag, UserRound, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { api } from '../services/api'
import { isAdmin, isCollaborator, type StoreInfo } from '../types/api'
import { useAuth } from '../app/AuthContext'
import { useCart } from '../app/CartContext'

export function Layout() {
  const [menu, setMenu] = useState(false); const [search, setSearch] = useState(''); const [store, setStore] = useState<StoreInfo | null>(null)
  const { user, logout } = useAuth(); const { count } = useCart(); const navigate = useNavigate()
  useEffect(() => { api.store().then(setStore).catch(() => undefined) }, [])
  const submit = (event: React.FormEvent) => { event.preventDefault(); navigate(`/catalogo?busca=${encodeURIComponent(search)}`); setMenu(false) }
  return <div className="app-shell">
    <a className="skip-link" href="#main">Pular para o conteúdo</a>
    <div className="announcement">Frete e pagamento serão confirmados após o pedido <span>•</span> Atendimento personalizado Vita Fortis</div>
    <header className="header">
      <div className="header-main container">
        <button className="menu-toggle" onClick={() => setMenu(!menu)} aria-label="Abrir menu">{menu ? <X /> : <Menu />}</button>
        <Link to="/" className="brand"><img src="/assets/imagens/logo_vitafortisBarraPesquisaSemFundo.png" alt="Vita Fortis" /></Link>
        <form className="search" onSubmit={submit}><Search size={18} /><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Busque suplemento, marca ou objetivo" aria-label="Buscar produtos" /></form>
        <div className="header-actions">
          <Link to={user ? '/conta' : '/entrar'} className="header-action"><UserRound /><span>{user ? user.nome.split(' ')[0] : 'Entrar'}</span></Link>
          <Link to="/sacola" className="header-action bag"><ShoppingBag /><span>Sacola</span>{count > 0 && <b>{count}</b>}</Link>
        </div>
      </div>
      <nav className={`nav ${menu ? 'open' : ''}`} aria-label="Navegação principal"><div className="container">
        <NavLink to="/catalogo" onClick={() => setMenu(false)}>Produtos</NavLink><NavLink to="/ofertas" onClick={() => setMenu(false)}>Ofertas</NavLink><NavLink to="/novidades" onClick={() => setMenu(false)}>Novidades</NavLink>{(!user || user.tipoUsuario==='CLIENTE')&&<><NavLink to="/favoritos" onClick={() => setMenu(false)}>Favoritos</NavLink><NavLink to="/pedidos" onClick={() => setMenu(false)}>Meus pedidos</NavLink></>}{isAdmin(user) && <NavLink to="/admin" onClick={() => setMenu(false)}>Painel admin</NavLink>}{isCollaborator(user)&&<NavLink to="/colaborador" onClick={()=>setMenu(false)}>Meu desempenho</NavLink>}
        {user && <button className="nav-logout" onClick={() => { logout(); navigate('/') }}>Sair</button>}
      </div></nav>
    </header>
    <main id="main"><Outlet /></main>
    <footer className="footer"><div className="container footer-grid"><div><img src="/assets/imagens/logo_vitafortisBarraPesquisaSemFundo.png" alt="" /><p>{store?.descricao || 'Suplementos para sua evolução, com escolhas mais simples e conscientes.'}</p></div><div><h3>Vita Fortis</h3><Link to="/catalogo">Catálogo</Link><Link to="/sobre">Sobre nós</Link><Link to="/favoritos">Favoritos</Link></div><div><h3>Ajuda</h3><Link to="/contato">Fale conosco</Link><Link to="/faq">Perguntas frequentes</Link><Link to="/politicas">Políticas</Link><Link to="/trabalhe-conosco">Trabalhe conosco</Link></div><div><h3>Atendimento</h3><p>{store?.telefone || 'Telefone em atualização'}</p><p>{store?.email || 'E-mail em atualização'}</p><p>{store?.endereco || 'Endereço em atualização'}</p></div></div><div className="container copyright">© {new Date().getFullYear()} Vita Fortis.</div></footer>
  </div>
}
