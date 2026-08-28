import { BadgeDollarSign, BarChart3, ReceiptText, TicketPercent } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { ErrorState, Empty, Loading } from '../components/States'
import { api } from '../services/api'
import { isCollaborator, type CollaboratorSummary } from '../types/api'
import { date, money, titleCase } from '../utils/format'

export function CollaboratorPage() {
  const { user } = useAuth()
  const [data,setData] = useState<CollaboratorSummary|null>(null)
  const [error,setError] = useState('')
  const load=()=>api.collaboratorSummary().then(setData).catch((e:Error)=>setError(e.message))
  useEffect(()=>{if(isCollaborator(user)) void load()},[user])
  if(!user) return <Navigate to="/entrar" replace/>
  if(!isCollaborator(user)) return <Navigate to={user.tipoUsuario==='ADMIN'?'/admin':'/conta'} replace/>
  return <div className="page container collaborator-page"><header className="page-header compact"><span className="eyebrow">Área do colaborador</span><h1>Olá, {user.nome.split(' ')[0]}.</h1><p>Acompanhe somente os cupons, vendas e cashback vinculados à sua conta.</p></header>
    {error?<ErrorState message={error} retry={load}/>:!data?<Loading/>:<>
      <div className="metric-grid"><article><BadgeDollarSign/><span>Saldo disponível</span><strong>{money(data.saldo)}</strong></article><article><BarChart3/><span>Vendas geradas</span><strong>{money(data.vendasGeradas)}</strong><small>{data.pedidosGerados} pedido(s)</small></article><article><ReceiptText/><span>Cashback confirmado</span><strong>{money(data.cashbackConfirmado)}</strong><small>{money(data.cashbackEstornado)} estornado</small></article></div>
      <div className="collaborator-grid"><section className="chart-card"><h2><TicketPercent size={18}/> Desempenho dos meus cupons</h2>{!data.cupons.length?<Empty title="Nenhum cupom vinculado" text="Solicite ao administrador o vínculo de um cupom."/>:<div className="table-wrap"><table><thead><tr><th>Cupom</th><th>Cashback</th><th>Pedidos</th><th>Vendas</th><th>Gerado</th></tr></thead><tbody>{data.cupons.map(c=><tr key={c.id}><td><strong>{c.codigo}</strong><small>{c.ativo?'Ativo':'Inativo'}</small></td><td>{c.percentualCashback}%</td><td>{c.pedidos}</td><td>{money(c.vendas)}</td><td>{money(c.cashback)}</td></tr>)}</tbody></table></div>}</section>
      <section className="chart-card"><h2>Extrato auditável</h2>{!data.movimentos.length?<Empty title="Sem movimentações" text="Créditos aparecem após a aprovação de pedidos."/>:<div className="movement-list">{data.movimentos.map(m=><article key={m.id}><div><strong>{titleCase(m.tipo)}</strong><small>{date(m.criadoEm)} {m.pedidoId&&`• Pedido #${m.pedidoId}`}</small><span>{m.justificativa}</span></div><b className={m.valor<0?'negative':'positive'}>{m.valor>0?'+':''}{money(m.valor)}</b></article>)}</div>}</section></div>
    </>}</div>
}
