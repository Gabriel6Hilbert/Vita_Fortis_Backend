import { BadgeDollarSign, ReceiptText, ShoppingBag } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { ErrorState, Loading } from '../components/States'
import { api } from '../services/api'
import { isCollaborator, type CollaboratorSummary } from '../types/api'
import { date, money, titleCase } from '../utils/format'
import '../styles/commerce-feedback.css'

const movementLabels: Record<string, string> = {
  CREDITO: 'Crédito de cashback',
  BAIXA: 'Baixa / valor gasto',
  ESTORNO: 'Estorno',
  AJUSTE: 'Ajuste de saldo',
}

export function CollaboratorPage() {
  const { user } = useAuth()
  const [data, setData] = useState<CollaboratorSummary | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [movementType, setMovementType] = useState('')
  const requestVersion = useRef(0)
  const load = () => {
    const version = ++requestVersion.current
    setError('')
    setLoading(true)
    api.collaboratorSummary()
      .then(result => { if (version === requestVersion.current) setData(result) })
      .catch((e: Error) => { if (version === requestVersion.current) setError(e.message) })
      .finally(() => { if (version === requestVersion.current) setLoading(false) })
  }
  useEffect(() => {
    setData(null)
    setMovementType('')
    if (isCollaborator(user)) load()
    return () => { requestVersion.current += 1 }
  }, [user])

  if (!user) return <Navigate to="/entrar" replace />
  if (!isCollaborator(user)) return <Navigate to={user.tipoUsuario === 'ADMIN' ? '/admin' : '/conta'} replace />

  const movements = (data?.movimentos || [])
    .filter(movement => !movementType || movement.tipo === movementType)
    .sort((first, second) => second.criadoEm.localeCompare(first.criadoEm) || second.id - first.id)

  return <div className="page container collaborator-page">
    <header className="page-header compact"><span className="eyebrow">Área do colaborador</span><h1>Olá, {user.nome.split(' ')[0]}.</h1><p>Acompanhe seu cashback e compre normalmente em toda a loja.</p><div className="profile-actions"><Link className="button primary" to="/catalogo"><ShoppingBag /> Comprar produtos</Link><Link className="button secondary" to="/pedidos">Meus pedidos</Link></div></header>
    {error ? <ErrorState message={error} retry={load} /> : loading || !data ? <Loading /> : <>
      <section className="cashback-summary-grid"><article className="cashback-balance-card"><BadgeDollarSign /><span>Saldo disponível de cashback</span><strong>{money(data.saldo)}</strong><p>Valor acumulado pelos pedidos elegíveis vinculados aos seus cupons.</p></article><article className="cashback-stats"><span>Cashback confirmado<strong>{money(data.cashbackConfirmado)}</strong></span><span>Cashback estornado<strong>{money(data.cashbackEstornado)}</strong></span><span>Pedidos gerados<strong>{data.pedidosGerados}</strong></span></article></section>
      <section className="chart-card cashback-statement">
        <header><div><h2><ReceiptText /> Extrato de cashback</h2><p>Histórico completo de créditos, estornos, ajustes e valores gastos.</p></div></header>
        <div className="cashback-statement-controls">
          <label htmlFor="cashback-movement-type">Tipo de movimentação<select id="cashback-movement-type" value={movementType} onChange={event => setMovementType(event.target.value)}><option value="">Todas as movimentações</option>{Object.entries(movementLabels).map(([type, label]) => <option key={type} value={type}>{label}</option>)}</select></label>
          <span role="status">{movements.length} {movements.length === 1 ? 'movimentação' : 'movimentações'} · mais recentes primeiro</span>
        </div>
        {!movements.length ? <p className="empty-inline">{movementType ? 'Nenhuma movimentação deste tipo. Selecione todas as movimentações para consultar o extrato completo.' : 'Nenhuma movimentação registrada. Créditos, baixas e ajustes aparecerão aqui quando forem confirmados.'}</p> :
          <div className="movement-list">{movements.map(movement => <article key={movement.id}>
            <div><strong>{movementLabels[movement.tipo] || titleCase(movement.tipo)}</strong><span>{movement.justificativa}{movement.pedidoId != null && ` · Pedido #${movement.pedidoId}`}</span><small>{date(movement.criadoEm)}</small></div>
            <div className="movement-value"><b className={movement.valor >= 0 ? 'positive' : 'negative'}>{movement.valor >= 0 ? '+' : ''}{money(movement.valor)}</b><small>Saldo anterior: {money(movement.saldoAnterior)}</small><small>Saldo após: {money(movement.saldoNovo)}</small></div>
          </article>)}</div>}
      </section>
    </>}
  </div>
}