import { ArrowRight, CheckCircle2, Heart, HelpCircle, Mail, MapPin, ShieldCheck, Sparkles } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ProductCard } from '../components/ProductCard'
import { Empty, ErrorState, Loading } from '../components/States'
import { api } from '../services/api'
import type { Product, StoreInfo } from '../types/api'
import { favoriteIds } from '../utils/commerce'

export function FavoritesPage() { const [products,setProducts] = useState<Product[]>([]); const [loading,setLoading] = useState(true); const [error,setError] = useState(''); const load = () => { setLoading(true); const session=JSON.parse(sessionStorage.getItem('vita-fortis-session')||'null'); const source=session?.user?.id?api.favorites(session.user.id):Promise.all(favoriteIds().map((id) => api.product(id))); source.then(setProducts).catch((e:Error) => setError(e.message)).finally(() => setLoading(false)) }; useEffect(load,[]); return <div className="page container"><header className="page-header"><Heart/><span className="eyebrow">Sua seleção</span><h1>Favoritos</h1><p>Produtos salvos na sua conta para você rever quando quiser.</p></header>{loading ? <Loading/> : error ? <ErrorState message={error} retry={load}/> : !products.length ? <Empty title="Nenhum favorito ainda" text="Use o coração nos produtos para montar sua seleção."/> : <div className="product-grid">{products.map((p) => <ProductCard key={p.id} product={p}/>)}</div>}</div> }

const content: Record<string,{title:string;eyebrow:string;body:string[]}> = {
  '/sobre': { title:'Sobre a Vita Fortis', eyebrow:'Nossa essência', body:['A Vita Fortis aproxima produtos, informação e atendimento humano para tornar escolhas de bem-estar mais simples.','Nossa proposta é acompanhar diferentes objetivos com clareza, procedência e uma experiência responsável.'] },
  '/politicas': { title:'Políticas da empresa', eyebrow:'Transparência', body:['As condições finais de pagamento, entrega e troca são confirmadas pela equipe no atendimento.','Para regras aplicáveis ao seu pedido, consulte a Vita Fortis pelos canais oficiais exibidos nesta página.'] },
  '/faq': { title:'Perguntas frequentes', eyebrow:'Como podemos ajudar?', body:['Como funciona o pedido? Você escolhe os itens, informa retirada ou entrega, revisa o total e seleciona o pagamento.','Como acompanho? Entre em sua conta e acesse Meus pedidos para consultar os status disponíveis.'] },
  '/trabalhe-conosco': { title:'Trabalhe conosco', eyebrow:'Faça parte', body:['Quer construir uma experiência de saúde e bem-estar mais próxima das pessoas? Envie sua apresentação pelos canais oficiais da loja.','Não coletamos currículo diretamente neste site enquanto não houver um canal seguro configurado.'] },
}
export function InstitutionalPage() { const { pathname } = useLocation(); const page = content[pathname] || content['/sobre']; return <div className="page container narrow"><header className="page-header"><span className="eyebrow">{page.eyebrow}</span><h1>{page.title}</h1></header><div className="prose-card">{page.body.map((p) => <p key={p}>{p}</p>)}{pathname === '/faq' && <HelpCircle/>}{pathname === '/politicas' && <ShieldCheck/>}</div></div> }
export function ContactPage() { const [store,setStore] = useState<StoreInfo|null>(null); useEffect(() => { api.store().then(setStore).catch(() => undefined) },[]); return <div className="page container"><header className="page-header"><span className="eyebrow">Fale conosco</span><h1>Atendimento próximo, do seu jeito.</h1><p>Use somente os canais oficiais retornados pela loja.</p></header><div className="contact-grid"><article><Mail/><h2>E-mail</h2><p>{store?.email || 'Canal em atualização'}</p></article><article><MapPin/><h2>Loja</h2><p>{store?.endereco || 'Endereço em atualização'}</p></article><article><CheckCircle2/><h2>Atendimento</h2><p>{store?.telefone || 'Telefone em atualização'}</p></article></div></div> }
export function CuratedPage() {
  const { pathname } = useLocation()
  const map:Record<string,{title:string;subtitle:string;filters:Record<string,string>;empty:string}> = {
    '/ofertas':{title:'Ofertas',subtitle:'Condições especiais selecionadas para você.',filters:{oferta:'true',ordenacao:'PRECO_ASC'},empty:'Nenhuma oferta disponível no momento.'},
    '/novidades':{title:'Novidades',subtitle:'Os lançamentos mais recentes da Vita Fortis.',filters:{lancamento:'true'},empty:'Nenhuma novidade cadastrada no momento.'},
    '/kits':{title:'Kits',subtitle:'Combinações para facilitar sua rotina.',filters:{categoria:'KITS'},empty:'Nenhum kit disponível no momento.'},
  }
  const selection=map[pathname]||map['/ofertas']; const [products,setProducts]=useState<Product[]>([]); const [loading,setLoading]=useState(true); const [error,setError]=useState('')
  const load=()=>{setLoading(true);setError('');api.products({...selection.filters,tamanho:48}).then(result=>setProducts(result.content)).catch((cause:Error)=>setError(cause.message)).finally(()=>setLoading(false))}
  useEffect(load,[pathname])
  const query=new URLSearchParams(selection.filters).toString()
  return <div className="page container curated-page"><header className="page-header"><Sparkles/><span className="eyebrow">Seleção Vita Fortis</span><h1>{selection.title}</h1><p>{selection.subtitle}</p></header>{loading?<Loading/>:error?<ErrorState message={error} retry={load}/>:products.length?<><div className="curated-summary"><strong>{products.length} {products.length===1?'produto selecionado':'produtos selecionados'}</strong><Link className="text-link" to={`/catalogo?${query}`}>Filtrar no catálogo <ArrowRight size={16}/></Link></div><div className="product-grid">{products.map(product=><ProductCard key={product.id} product={product}/>)}</div></>:<Empty title={selection.empty} text="Volte em breve ou explore o catálogo completo."/>}</div>
}
