import { ArrowRight, BadgePercent, CheckCircle2, Dumbbell, Heart, HelpCircle, Layers3, Mail, MapPin, PackageCheck, Sparkles, Tags, Target } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ProductCard } from '../components/ProductCard'
import { Empty, ErrorState, Loading } from '../components/States'
import { api } from '../services/api'
import type { BrandSummary, GoalSummary, Product, StoreInfo } from '../types/api'
import { favoriteIds } from '../utils/commerce'

export function FavoritesPage() { const [products,setProducts] = useState<Product[]>([]); const [loading,setLoading] = useState(true); const [error,setError] = useState(''); const load = () => { setLoading(true); const session=JSON.parse(sessionStorage.getItem('vita-fortis-session')||'null'); const source=session?.user?.id?api.favorites(session.user.id):Promise.all(favoriteIds().map((id) => api.product(id))); source.then(setProducts).catch((e:Error) => setError(e.message)).finally(() => setLoading(false)) }; useEffect(load,[]); return <div className="page container"><header className="page-header"><Heart/><span className="eyebrow">Sua seleção</span><h1>Favoritos</h1><p>Produtos salvos na sua conta para você rever quando quiser.</p></header>{loading ? <Loading/> : error ? <ErrorState message={error} retry={load}/> : !products.length ? <Empty title="Nenhum favorito ainda" text="Use o coração nos produtos para montar sua seleção."/> : <div className="product-grid">{products.map((p) => <ProductCard key={p.id} product={p}/>)}</div>}</div> }

const content: Record<string,{title:string;eyebrow:string;body:string[]}> = {
  '/sobre': { title:'Sobre a Vita Fortis', eyebrow:'Nossa essência', body:['A Vita Fortis aproxima produtos, informação e atendimento humano para tornar escolhas de bem-estar mais simples.','Nossa proposta é acompanhar diferentes objetivos com clareza, procedência e uma experiência responsável.'] },
  '/faq': { title:'Perguntas frequentes', eyebrow:'Como podemos ajudar?', body:['Como funciona o pedido? Você escolhe os itens, informa retirada ou entrega, revisa o total e seleciona o pagamento.','Como acompanho? Entre em sua conta e acesse Meus pedidos para consultar os status disponíveis.'] },
  '/trabalhe-conosco': { title:'Trabalhe conosco', eyebrow:'Faça parte', body:['Quer construir uma experiência de saúde e bem-estar mais próxima das pessoas? Envie sua apresentação pelos canais oficiais da loja.','Não coletamos currículo diretamente neste site enquanto não houver um canal seguro configurado.'] },
}
export function InstitutionalPage() { const { pathname } = useLocation(); const page = content[pathname] || content['/sobre']; return <div className="page container narrow"><header className="page-header"><span className="eyebrow">{page.eyebrow}</span><h1>{page.title}</h1></header><div className="prose-card">{page.body.map((p) => <p key={p}>{p}</p>)}{pathname === '/faq' && <HelpCircle/>}</div></div> }
export function ContactPage() { const [store,setStore] = useState<StoreInfo|null>(null); useEffect(() => { api.store().then(setStore).catch(() => undefined) },[]); return <div className="page container"><header className="page-header"><span className="eyebrow">Fale conosco</span><h1>Atendimento próximo, do seu jeito.</h1><p>Use somente os canais oficiais retornados pela loja.</p></header><div className="contact-grid"><article><Mail/><h2>E-mail</h2><p>{store?.email || 'Canal em atualização'}</p></article><article><MapPin/><h2>Loja</h2><p>{store?.endereco || 'Endereço em atualização'}</p></article><article><CheckCircle2/><h2>Atendimento</h2><p>{store?.telefone || 'Telefone em atualização'}</p></article></div></div> }
export function BrandsPage() {
  const [items,setItems]=useState<BrandSummary[]>([])
  const [loading,setLoading]=useState(true)
  const [error,setError]=useState('')
  const load=()=>{setLoading(true);setError('');api.brands().then(setItems).catch((cause:Error)=>setError(cause.message)).finally(()=>setLoading(false))}
  useEffect(load,[])
  const productCount=items.reduce((sum,item)=>sum+item.quantidadeProdutos,0)
  return <main className="page container discovery-page brands-page">
    <header className="discovery-hero">
      <div className="discovery-hero-copy"><span className="eyebrow"><Tags size={14}/> Catálogo por marca</span><h1>Encontre sua marca favorita.</h1><p>Compare linhas, fórmulas e opções de cada fabricante em poucos cliques.</p></div>
      <div className="discovery-stat"><strong>{items.length}</strong><span>marcas ativas</span><small>{productCount} produtos disponíveis</small></div>
    </header>
    {loading?<Loading label="Carregando marcas…"/>:error?<ErrorState message={error} retry={load}/>:<section className="brand-directory" aria-label="Marcas disponíveis">
      <div className="directory-heading"><div><span className="eyebrow">Todas as marcas</span><h2>Escolha por fabricante</h2></div><Link className="text-link" to="/catalogo">Ver catálogo completo <ArrowRight size={16}/></Link></div>
      <div className="selection-grid brand-grid">{items.map((item,index)=><Link key={item.nome} to={`/catalogo?marca=${encodeURIComponent(item.nome)}`}><span className="brand-index">{String(index+1).padStart(2,'0')}</span><strong>{item.nome}</strong><span>{item.quantidadeProdutos} {item.quantidadeProdutos===1?'produto':'produtos'}</span><ArrowRight/></Link>)}</div>
    </section>}
  </main>
}
const goalVisuals:Record<string,{image:string;description:string}> = {
  EMAGRECIMENTO:{image:'/assets/imagens/objetivos/emagrecimento.webp',description:'Escolhas para uma rotina equilibrada e consistente.'},
  GANHO_DE_MASSA:{image:'/assets/imagens/objetivos/ganho-massa.webp',description:'Proteínas, energia e suporte para sua evolução.'},
  PERFORMANCE:{image:'/assets/imagens/objetivos/performance.webp',description:'Potência e recuperação para ir mais longe.'},
  SAUDE_E_BEM_ESTAR:{image:'/assets/imagens/objetivos/saude-bem-estar.webp',description:'Cuidado diário para corpo, mente e disposição.'},
}
export function GoalsPage() {
  const [items,setItems]=useState<GoalSummary[]>([])
  const [loading,setLoading]=useState(true)
  const [error,setError]=useState('')
  const load=()=>{setLoading(true);setError('');api.goals().then(setItems).catch((cause:Error)=>setError(cause.message)).finally(()=>setLoading(false))}
  useEffect(load,[])
  const productCount=items.reduce((sum,item)=>sum+item.quantidadeProdutos,0)
  return <main className="page container discovery-page goals-page">
    <header className="discovery-hero goals-hero">
      <div className="discovery-hero-copy"><span className="eyebrow"><Target size={14}/> Eu quero...</span><h1>Comece pelo seu objetivo.</h1><p>Escolha o resultado que você busca e encontre uma seleção mais simples para a sua rotina.</p></div>
      <div className="discovery-stat"><strong>{items.length}</strong><span>objetivos principais</span><small>{productCount} produtos relacionados</small></div>
    </header>
    {loading?<Loading label="Carregando objetivos…"/>:error?<ErrorState message={error} retry={load}/>:<section className="goals-directory" aria-label="Objetivos disponíveis">
      <div className="directory-heading"><div><span className="eyebrow">Escolha seu caminho</span><h2>O que você quer conquistar?</h2></div><Link className="text-link" to="/catalogo">Explorar tudo <ArrowRight size={16}/></Link></div>
      <div className="goal-selection-grid">{items.map((item,index)=>{const visual=goalVisuals[item.codigo]||Object.values(goalVisuals)[index%4];return <Link key={item.codigo} to={`/catalogo?objetivos=${item.codigo}`} className="goal-selection-card"><img src={visual.image} alt=""/><span className="goal-selection-shade"/><span className="goal-selection-count">{item.quantidadeProdutos} {item.quantidadeProdutos===1?'produto':'produtos'}</span><span className="goal-selection-copy"><strong>{item.nome}</strong><small>{visual.description}</small></span><span className="goal-selection-action" aria-hidden="true"><ArrowRight/></span></Link>})}</div>
    </section>}
  </main>
}
export function CuratedPage() {
  const { pathname } = useLocation()
  const map:Record<string,{title:string;headline:string;subtitle:string;filters:Record<string,string>;empty:string;metric:string;icon:typeof Sparkles}> = {
    '/ofertas':{title:'Ofertas',headline:'Mais performance, melhor preço.',subtitle:'Condições especiais em produtos selecionados para você aproveitar agora.',filters:{oferta:'true',ordenacao:'PRECO_ASC'},empty:'Nenhuma oferta disponível no momento.',metric:'ofertas selecionadas',icon:BadgePercent},
    '/novidades':{title:'Novidades',headline:'Acabou de chegar por aqui.',subtitle:'Conheça os lançamentos mais recentes da curadoria Vita Fortis.',filters:{lancamento:'true'},empty:'Nenhuma novidade cadastrada no momento.',metric:'novidades na vitrine',icon:Sparkles},
    '/kits':{title:'Kits',headline:'Sua rotina pronta em um só kit.',subtitle:'Combinações pensadas para facilitar seus objetivos e render mais no dia a dia.',filters:{kit:'true'},empty:'Nenhum kit disponível no momento.',metric:'combinações disponíveis',icon:PackageCheck},
  }
  const selection=map[pathname]||map['/ofertas']; const [products,setProducts]=useState<Product[]>([]); const [loading,setLoading]=useState(true); const [error,setError]=useState('')
  const load=()=>{setLoading(true);setError('');api.products({...selection.filters,tamanho:48}).then(result=>setProducts(result.content)).catch((cause:Error)=>setError(cause.message)).finally(()=>setLoading(false))}
  useEffect(load,[pathname])
  const query=new URLSearchParams(selection.filters).toString()
  const Icon=selection.icon
  return <main className={`page container discovery-page curated-page curated-${pathname.slice(1)}`}>
    <header className="discovery-hero curated-hero">
      <div className="discovery-hero-copy"><span className="eyebrow"><Icon size={14}/> Seleção Vita Fortis</span><h1>{selection.headline}</h1><p>{selection.subtitle}</p><Link className="button primary" to={`/catalogo?${query}`}>Explorar no catálogo <ArrowRight size={17}/></Link></div>
      <div className="curated-hero-mark" aria-hidden="true"><Icon/><span>{selection.title}</span></div>
    </header>
    {loading?<Loading label={`Carregando ${selection.title.toLowerCase()}…`}/>:error?<ErrorState message={error} retry={load}/>:products.length?<section className="curated-products"><div className="directory-heading"><div><span className="eyebrow">Curadoria ativa</span><h2>{products.length} {products.length===1?'item selecionado':selection.metric}</h2></div><div className="curated-benefits"><span><Dumbbell size={16}/> Para sua rotina</span><span><Layers3 size={16}/> Seleção objetiva</span></div></div><div className="product-grid">{products.map(product=><ProductCard key={product.id} product={product}/>)}</div></section>:<Empty title={selection.empty} text="Volte em breve ou explore o catálogo completo."/>}
  </main>
}
