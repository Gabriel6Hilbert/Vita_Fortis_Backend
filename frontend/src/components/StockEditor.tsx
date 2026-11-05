import { useEffect, useState } from 'react'
import { api } from '../services/api'
import type { Product, StockMovement } from '../types/api'
import { date } from '../utils/format'

export function StockEditor({ product, close, onSaved }: {
  product: Product
  close: () => void
  onSaved: (product: Product) => void
}) {
  const [quantity, setQuantity] = useState(String(product.quantidadeEstoque))
  const [reason, setReason] = useState('')
  const [history, setHistory] = useState<StockMovement[]>([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [historyError, setHistoryError] = useState('')
  const [success, setSuccess] = useState('')

  async function reloadHistory(id: number) {
    setHistoryError('')
    try { setHistory(await api.stockHistory(id)) }
    catch { setHistoryError('Não foi possível carregar o histórico. Tente consultar novamente.') }
  }

  useEffect(() => { void reloadHistory(product.id) }, [product.id])

  return <div className="modal-backdrop">
    <form className="admin-modal small" aria-busy={busy} onSubmit={async event => {
      event.preventDefault()
      if (busy) return
      const value = Number(quantity)
      if (!quantity || !Number.isInteger(value) || value < 0 || value > 2147483647 || !reason.trim()) {
        setError('Informe um saldo inteiro não negativo e o motivo do ajuste.')
        return
      }
      setBusy(true)
      setError('')
      setSuccess('')
      try {
        const saved = await api.setStock(product.id, value, reason.trim())
        onSaved(saved)
        setReason('')
        setSuccess('Saldo atualizado.')
        await reloadHistory(product.id)
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : 'Não foi possível ajustar o estoque.')
      } finally { setBusy(false) }
    }}>
      <header><h2>Ajustar estoque</h2></header>
      <p>{product.nome} · Saldo atual: <strong>{product.quantidadeEstoque} unidades</strong></p>
      <div className="form-grid">
        <label>Novo saldo<input type="number" min="0" max="2147483647" step="1" required disabled={busy} value={quantity} onChange={event => setQuantity(event.target.value)} /></label>
        <label>Motivo<input required maxLength={255} disabled={busy} value={reason} onChange={event => setReason(event.target.value)} /></label>
      </div>
      {error && <p role="alert" className="form-error">{error}</p>}
      {success && <p role="status" className="form-success">{success}</p>}
      <h3>Movimentações</h3>
      {historyError ? <p role="alert">{historyError} <button type="button" className="text-action" onClick={() => void reloadHistory(product.id)}>Recarregar histórico</button></p>
        : <ul>{history.map(entry => <li key={entry.id}>{date(entry.criadoEm)} · {entry.quantidadeAnterior} → {entry.quantidadeNova} · {entry.motivo} · {entry.responsavel}</li>)}</ul>}
      <footer>
        <button type="button" className="button secondary" disabled={busy} onClick={close}>Fechar</button>
        <button className="button primary" disabled={busy}>{busy ? 'Salvando…' : 'Salvar ajuste'}</button>
      </footer>
    </form>
  </div>
}
