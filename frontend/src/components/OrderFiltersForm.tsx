import { useState } from 'react'
import type { OrderFilters } from '../services/api'
import { titleCase } from '../utils/format'
import '../styles/order-filters.css'

export function OrderFiltersForm({ applied, loading, onApply }: {
  applied: OrderFilters; loading: boolean; onApply: (filters: OrderFilters) => void
}) {
  const [draft, setDraft] = useState<OrderFilters>(applied)
  const invalidPeriod = Boolean(draft.inicio && draft.fim && draft.fim < draft.inicio)
  const field = (name: keyof OrderFilters, value: string) => setDraft(current => ({ ...current, [name]: value }))
  return <form className="order-filters" onSubmit={event => {
    event.preventDefault()
    if (!invalidPeriod) onApply(draft)
  }}>
    <fieldset disabled={loading}>
      <legend>Consultar pedidos</legend>
      <div className="order-filters-grid">
        <label>Número do pedido<input value={draft.numero || ''} onChange={e => field('numero', e.target.value)} placeholder="Número ou parte dele" /></label>
        <label>Data inicial do pedido<input type="date" value={draft.inicio || ''} onChange={e => field('inicio', e.target.value)} aria-invalid={invalidPeriod} aria-describedby={invalidPeriod ? 'order-period-error' : undefined} /></label>
        <label>Data final do pedido<input type="date" value={draft.fim || ''} onChange={e => field('fim', e.target.value)} aria-invalid={invalidPeriod} aria-describedby={invalidPeriod ? 'order-period-error' : undefined} /></label>
        <label>Cliente<input value={draft.cliente || ''} onChange={e => field('cliente', e.target.value)} placeholder="Nome ou e-mail" /></label>
        <label>Status do pedido<select value={draft.status || ''} onChange={e => field('status', e.target.value)}><option value="">Todos</option>{['PENDENTE', 'PAGAMENTO_APROVADO', 'EM_SEPARACAO', 'ENVIADO', 'ENTREGUE', 'CANCELADO'].map(value => <option key={value} value={value}>{titleCase(value)}</option>)}</select></label>
        <label>Forma de recebimento<select value={draft.recebimento || ''} onChange={e => field('recebimento', e.target.value)}><option value="">Todas</option><option value="RETIRADA">Retirada</option><option value="ENTREGA">Entrega</option></select></label>
        <label>Forma de pagamento<select value={draft.pagamento || ''} onChange={e => field('pagamento', e.target.value)}><option value="">Todas</option><option value="PIX">Pix</option><option value="CARTAO">Cartão</option><option value="BOLETO">Boleto</option></select></label>
        <label>Situação do pagamento<select value={draft.statusPagamento || ''} onChange={e => field('statusPagamento', e.target.value)}><option value="">Todas</option>{['PENDENTE', 'APROVADO', 'RECUSADO', 'ESTORNADO'].map(value => <option key={value} value={value}>{titleCase(value)}</option>)}</select></label>
      </div>
      {invalidPeriod && <p id="order-period-error" role="alert">A data final deve ser igual ou posterior à data inicial.</p>}
      <div className="order-filter-actions"><button className="button primary" disabled={invalidPeriod}>Aplicar filtros</button><button type="button" className="button secondary" onClick={() => { setDraft({}); onApply({}) }}>Limpar filtros</button></div>
    </fieldset>
  </form>
}
