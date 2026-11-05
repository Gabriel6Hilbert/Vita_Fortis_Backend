import { BadgeDollarSign, ShoppingBag } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { ErrorState, Loading } from '../components/States'
import { ApiError, api } from '../services/api'
import { isCollaborator, type CollaboratorReport, type CollaboratorSummary } from '../types/api'
import { money } from '../utils/format'

export function CollaboratorPage() {
  const { user } = useAuth()
  const [data, setData] = useState<CollaboratorSummary | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const requestVersion = useRef(0)
  const load = () => {
    const version = ++requestVersion.current
    setError(''); setLoading(true)
    api.collaboratorSummary().then(result => { if (version === requestVersion.current) setData(result) })
      .catch((e: Error) => { if (version === requestVersion.current) setError(e.message) })
      .finally(() => { if (version === requestVersion.current) setLoading(false) })
  }
  useEffect(() => { setData(null); if (isCollaborator(user)) load(); return () => { requestVersion.current += 1 } }, [user])

  if (!user) return <Navigate to="/entrar" replace />
  if (!isCollaborator(user)) return <Navigate to={user.tipoUsuario === 'ADMIN' ? '/admin' : '/conta'} replace />

  const choose = async (destino: 'PIX' | 'DESCONTO') => {
    setError('')
    try { setData(await api.chooseCashbackDestination(destino)) }
    catch (e) { setError(e instanceof Error ? e.message : 'Não foi possível salvar a escolha.') }
  }

  return <div className="page container collaborator-page">
    <header className="page-header compact"><span className="eyebrow">Área do colaborador</span><h1>Olá, {user.nome.split(' ')[0]}.</h1><p>Consulte seu saldo e escolha como deseja utilizá-lo.</p><div className="profile-actions"><Link className="button primary" to="/catalogo"><ShoppingBag /> Comprar produtos</Link><Link className="button secondary" to="/pedidos">Meus pedidos</Link></div></header>
    {error ? <ErrorState message={error} retry={load} /> : loading || !data ? <Loading /> : <>
      <article className="cashback-balance-card"><BadgeDollarSign /><span>Saldo disponível de cashback</span><strong>{money(data.saldo)}</strong><p>Valor acumulado pelos pedidos elegíveis e confirmados.</p></article>
      <section className="chart-card"><h2>Como receber ou usar o cashback</h2><p>Sua escolha fica salva e pode ser alterada.</p><label>Destino do cashback<select value={data.destinoCashback || ''} onChange={event => event.target.value && choose(event.target.value as 'PIX' | 'DESCONTO')}><option value="" disabled>Escolha uma opção</option><option value="PIX">Receber por Pix</option><option value="DESCONTO">Usar como desconto em compras</option></select></label></section>
    </>}
    {user.permissaoRelatorios && <OwnReports key={user.id} />}
  </div>
}

const movementLabels = { CREDITO: 'Crédito', ESTORNO: 'Estorno', AJUSTE: 'Ajuste', BAIXA: 'Baixa' }

function OwnReports() {
  const [report, setReport] = useState<CollaboratorReport | null>(null)
  const [error, setError] = useState('')
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let current = true
    api.collaboratorReport().then(result => {
      if (current) setReport(result)
    }).catch((failure: unknown) => {
      if (!current) return
      setError(failure instanceof ApiError && failure.status === 403
        ? 'Você não tem permissão para consultar relatórios.'
        : 'Não foi possível carregar seus relatórios.')
    })
    return () => { current = false }
  }, [attempt])
  if (error) return <ErrorState message={error} retry={() => { setError(''); setAttempt(value => value + 1) }} />
  if (!report) return <Loading label="Carregando relatórios…" />
  return <section className="chart-card">
    <h2>Meus relatórios</h2>
    <h3>Extrato de cashback</h3>
    {report.movimentos.length ? <ul>{report.movimentos.map(item => <li key={item.id}>
      {new Date(item.criadoEm).toLocaleDateString('pt-BR')} · {movementLabels[item.tipo]} · {money(item.valor)}
    </li>)}</ul> : <p>Nenhum lançamento.</p>}
    <h3>Cupons vinculados</h3>
    {report.cupons.length ? <ul>{report.cupons.map(cupom => <li key={cupom.codigo}>
      {cupom.codigo} · {cupom.ativo ? 'Ativo' : 'Inativo'}{cupom.percentualCashback !== null && ` · Cashback: ${cupom.percentualCashback}%`}
    </li>)}</ul> : <p>Nenhum cupom vinculado.</p>}
  </section>
}
