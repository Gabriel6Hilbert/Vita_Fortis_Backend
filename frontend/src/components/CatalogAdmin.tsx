import { useState } from 'react'
import { api } from '../services/api'
import type { CatalogRegistration } from '../types/api'

export function CatalogAdmin({ items, reload }: { items: CatalogRegistration[]; reload: () => Promise<void> }) {
  const [form, setForm] = useState<Partial<CatalogRegistration>>({ tipo: 'CATEGORIA', ativo: true })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const run = async (operation: () => Promise<unknown>) => {
    setBusy(true); setError('')
    try { await operation(); await reload(); setForm({ tipo: form.tipo, ativo: true }) }
    catch (e) { setError(e instanceof Error ? e.message : 'Falha ao salvar cadastro.') }
    finally { setBusy(false) }
  }
  return <section>
    <h2>Categorias e atributos</h2>
    <p>Cadastre categorias e características livres dos produtos. Inativar mantém os vínculos existentes; reative pelo botão Editar.</p>
    {error && <p role="alert" className="form-error">{error}</p>}
    <form className="form-grid" onSubmit={e => { e.preventDefault(); void run(() => api.saveCatalogRegistration(form)) }}>
      <label>Tipo<select disabled={!!form.id} value={form.tipo} onChange={e => setForm({ ...form, tipo: e.target.value as CatalogRegistration['tipo'] })}><option value="CATEGORIA">Categoria</option><option value="ATRIBUTO">Atributo</option></select></label>
      <label>Código<input required disabled={!!form.id} maxLength={40} pattern="[A-Z0-9_]+" placeholder="Ex.: SABOR" value={form.codigo || ''} onChange={e => setForm({ ...form, codigo: e.target.value.toUpperCase() })} /></label>
      <label>Nome<input required maxLength={120} value={form.nome || ''} onChange={e => setForm({ ...form, nome: e.target.value })} /></label>
      <label>Situação<select value={String(form.ativo)} onChange={e => setForm({ ...form, ativo: e.target.value === 'true' })}><option value="true">Ativo</option><option value="false">Inativo</option></select></label>
      <button className="button primary" disabled={busy}>{busy ? 'Salvando…' : form.id ? 'Salvar alterações' : 'Cadastrar'}</button>
      {form.id && <button type="button" className="button secondary" onClick={() => setForm({ tipo: 'CATEGORIA', ativo: true })}>Cancelar edição</button>}
    </form>
    <div className="table-wrap"><table><thead><tr><th>Tipo</th><th>Código</th><th>Nome</th><th>Situação</th><th>Ações</th></tr></thead><tbody>{items.map(item => <tr key={item.id}><td>{item.tipo === 'CATEGORIA' ? 'Categoria' : 'Atributo'}</td><td>{item.codigo}</td><td>{item.nome}</td><td>{item.ativo ? 'Ativo' : 'Inativo'}</td><td><button className="text-action" disabled={busy} onClick={() => setForm(item)}>Editar</button>{item.ativo && <button className="text-action" disabled={busy} onClick={() => { if (confirm(`Inativar ${item.nome}? Os vínculos existentes serão preservados.`)) void run(() => api.disableCatalogRegistration(item.id)) }}>Inativar</button>}</td></tr>)}</tbody></table></div>
  </section>
}
