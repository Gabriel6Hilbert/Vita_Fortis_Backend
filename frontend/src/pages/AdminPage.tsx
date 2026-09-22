import {
  BarChart3,
  Boxes,
  Copy,
  Download,
  Eye,
  MessageSquare,
  PackageCheck,
  Pencil,
  Plus,
  ImagePlus,
  Search,
  ShieldAlert,
  TicketPercent,
  UserPlus,
  Users,
  WalletCards,
  X,
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../app/AuthContext";
import { ErrorState, Loading } from "../components/States";
import { api } from "../services/api";
import type { OrderFilters } from "../services/api";
import { CatalogAdmin } from "../components/CatalogAdmin";
import type { CatalogRegistration } from "../types/api";
import { OrderFiltersForm } from "../components/OrderFiltersForm";
import { PrivacyAdmin } from "../components/PrivacyAdmin";
import {
  isAdmin,
  type AdminMetrics,
  type Coupon,
  type Order,
  type Product,
  type ImportHistory,
  type Review,
  type User,
} from "../types/api";
import { date, money, titleCase } from "../utils/format";
import "../styles/commerce-feedback.css";
type Tab =
  | "dashboard"
  | "products"
  | "catalog"
  | "orders"
  | "users"
  | "coupons"
  | "reviews"
  | "privacy"
  | "reports";
const blankProduct: Partial<Product> = {
  codigo: "",
  nome: "",
  descricao: "",
  marca: "",
  unidade: "",
  preco: 0,
  quantidadeEstoque: 0,
  categoria: "",
  imagemUrl: "",
  ativo: true,
  lancamento: false,
  destaque: false,
  oferta: false,
  kit: false,
  valorDesconto: 0,
};
export function AdminPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState<Tab>("dashboard");
  const [registrations, setRegistrations] = useState<CatalogRegistration[]>([]);
  const [couponError, setCouponError] = useState("");
  const [couponSaving, setCouponSaving] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [orderFilters, setOrderFilters] = useState<OrderFilters>({});
  const [users, setUsers] = useState<User[]>([]);
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [metrics, setMetrics] = useState<AdminMetrics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [search, setSearch] = useState("");
  const [productForm, setProductForm] = useState<Partial<Product> | null>(null);
  const [imageUploading,setImageUploading]=useState(false);
  const [imageError, setImageError] = useState("");
  const [imageStatus, setImageStatus] = useState("");
  const [productSaving, setProductSaving] = useState(false);
  const [productError, setProductError] = useState("");
  const [importFile,setImportFile]=useState<File|null>(null);
  const [importPreview,setImportPreview]=useState<{inseridos:number;atualizados:number;rejeitados:number;erros:string[];registros:{codigo:string;nome:string;preco:number;estoque:number;categoria:string;acao:string}[]}|null>(null);
  const [importHistory,setImportHistory]=useState<ImportHistory[]>([]);
  const [orderOpen, setOrderOpen] = useState<Order | null>(null);
  const [couponForm, setCouponForm] = useState<Partial<Coupon> | null>(null);
  const [cashbackForm, setCashbackForm] = useState<{
    id: number;
    nome: string;
    tipo: "AJUSTE" | "BAIXA";
    valor: number;
    justificativa: string;
  } | null>(null);
  const [reportPeriod, setReportPeriod] = useState(() => {
    const now = new Date();
    return {
      inicio: new Date(now.getFullYear(), now.getMonth(), 1)
        .toISOString()
        .slice(0, 10),
      fim: now.toISOString().slice(0, 10),
    };
  });
  const [reportType,setReportType]=useState('VENDAS');
  const [reportFilters,setReportFilters]=useState<Record<string,string>>({});
  const [reportDownloading,setReportDownloading]=useState(false);
  const filterReport=(name:string,value:string)=>setReportFilters(current=>({...current,[name]:value}));
  const [reportFormat,setReportFormat]=useState<'csv'|'pdf'|'xlsx'>('csv');
  const [collaboratorForm, setCollaboratorForm] = useState({
    nome: "",
    email: "",
    senha: "",
    cpf: "",
    telefone: "",
  });
  const selectPeriod = (days: number | "month") => {
    const end = new Date();
    const start =
      days === "month"
        ? new Date(end.getFullYear(), end.getMonth(), 1)
        : new Date(end.getFullYear(), end.getMonth(), end.getDate() - days + 1);
    const localDate = (value: Date) => {
      const offset = value.getTimezoneOffset() * 60_000;
      return new Date(value.getTime() - offset).toISOString().slice(0, 10);
    };
    setReportPeriod({ inicio: localDate(start), fim: localDate(end) });
  };
  const load = async (filters: OrderFilters = orderFilters) => {
    if (tab === "dashboard" && reportPeriod.fim < reportPeriod.inicio) {
      setError("A data final deve ser igual ou posterior à data inicial.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      if (["products", "reports", "catalog"].includes(tab)) setRegistrations(await api.catalogRegistrations());
      if (tab === "dashboard")
        setMetrics(await api.metrics(reportPeriod.inicio, reportPeriod.fim));
      if (tab === "products") {
        const [productPage, history] = await Promise.all([
          api.adminProducts({ tamanho: 500, busca: search || undefined }),
          api.importHistory(),
        ]);
        setProducts(productPage.content);
        setImportHistory(history);
      }
      if (tab === "orders") setOrders(await api.adminOrders(filters));
      if (tab === "users" || tab === "reports") setUsers(await api.adminUsers());
      if (tab === "coupons") {
        const [couponList, userList] = await Promise.all([
          api.coupons(),
          api.adminUsers(),
        ]);
        setCoupons(couponList);
        setUsers(userList);
      }
      if (tab === "reviews") setReviews(await api.adminReviews());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao carregar o painel.");
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    void load();
  }, [tab]);
  useEffect(() => { setSearch('') }, [tab]);
  const visibleProducts = useMemo(
    () =>
      products.filter((p) =>
        `${p.nome} ${p.codigo} ${p.marca}`
          .toLowerCase()
          .includes(search.toLowerCase()),
      ),
    [products, search],
  );
  if (!user) return <Navigate to="/entrar" replace />;
  if (!isAdmin(user))
    return (
      <div className="page container">
        <div className="state error">
          <ShieldAlert />
          <h1>Acesso restrito</h1>
          <p>Área exclusiva para administradores.</p>
        </div>
      </div>
    );
  const action = async (
    run: () => Promise<unknown>,
    message = "Alteração salva com sucesso.",
  ) => {
    try {
      setError("");
      await run();
      setSuccess(message);
      setTimeout(() => setSuccess(""), 2500);
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Ação não concluída.");
    }
  };
  const openProductForm = (form: Partial<Product> | null) => {
    setImageError("");
    setImageStatus("");
    setProductError("");
    setProductForm(form);
  };
  const submitProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productForm || imageUploading || productSaving) return;
    const form = productForm;
    setProductSaving(true);
    setProductError("");
    try {
        const saved = await api.saveProduct(form, form.id);
        // Keep the saved ID if a later metadata request fails, so retry updates it.
        setProductForm(current => current ? { ...current, id: saved.id } : current);
        if (Number(form.descontoPercentual) > 0)
          await api.setDiscountPercent(
            saved.id,
            Number(form.descontoPercentual),
          );
        else if (Number(form.descontoValor) > 0)
          await api.setDiscountValue(saved.id, Number(form.descontoValor));
        else if (form.id) await api.clearDiscount(saved.id);
        await api.setCommercialMetadata(saved.id, {
          objetivos: form.objetivos,
          esportes: form.esportes,
          vegano: form.vegano,
          vegetariano: form.vegetariano,
          linhaClinica: form.linhaClinica,
          lancamento: form.lancamento,
          destaque: form.destaque,
          oferta: form.oferta,
          kit: form.kit,
          subcategoria: form.subcategoria,
          avaliacaoMedia: form.avaliacaoMedia,
        });
      setSuccess(form.id ? "Produto atualizado." : "Produto criado.");
      setTimeout(() => setSuccess(""), 2500);
      openProductForm(null);
      await load();
    } catch (e) {
      setProductError(e instanceof Error ? e.message : "Não foi possível salvar o produto. Seus dados foram mantidos.");
    } finally {
      setProductSaving(false);
    }
  };
  const uploadProductImage = async (file: File) => {
    if (imageUploading || productSaving) return;
    setImageError("");
    setImageStatus("");
    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      setImageError("Selecione uma imagem JPG, PNG ou WebP.");
      return;
    }
    if (file.size === 0 || file.size > 5 * 1024 * 1024) {
      setImageError(file.size === 0 ? "O arquivo está vazio. Selecione outra imagem." : "A imagem deve ter no máximo 5 MB.");
      return;
    }
    setImageUploading(true);
    try {
      const uploaded = await api.uploadProductImage(file);
      setProductForm(current => current ? { ...current, imagemUrl: uploaded.url } : current);
      setImageStatus("Imagem enviada. Salve o produto para confirmar a alteração.");
    } catch (e) {
      setImageError(e instanceof Error ? e.message : "Não foi possível enviar a imagem. Tente novamente.");
    } finally {
      setImageUploading(false);
    }
  };
  const submitCoupon = async (e: React.FormEvent) => {
    e.preventDefault(); if (!couponForm || couponSaving) return;
    setCouponSaving(true); setCouponError('');
    try { await api.saveCoupon({...couponForm, dataInicio: couponForm.dataInicio || null, dataVencimento: couponForm.dataVencimento || null, limiteUso: couponForm.limiteUso || null}, couponForm.id); setCouponForm(null); setSuccess('Cupom salvo.'); await load(); }
    catch (e) { setCouponError(e instanceof Error ? e.message : 'Erro ao salvar cupom.'); }
    finally { setCouponSaving(false); }
  };
  return (
    <div className="admin-shell">
      <aside className="admin-nav">
        <div>
          <span>Vita Fortis</span>
          <strong>Administração</strong>
        </div>
        {(
          [
            ["dashboard", BarChart3, "Visão geral"],
            ["products", Boxes, "Produtos"],
            ["catalog", Boxes, "Categorias e atributos"],
            ["orders", PackageCheck, "Pedidos"],
            ["users", Users, "Usuários"],
            ["coupons", TicketPercent, "Cupons"],
            ["reviews", MessageSquare, "Avaliações"],
            ["privacy", ShieldAlert, "Privacidade"],
            ["reports", Download, "Relatórios"],
          ] as const
        ).map(([value, Icon, label]) => (
          <button
            key={value}
            className={tab === value ? "active" : ""}
            onClick={() => setTab(value)}
          >
            <Icon /> {label}
          </button>
        ))}
      </aside>
      <section className="admin-content">
        <header>
          <span className="eyebrow">Painel operacional</span>
          <h1>{tab === "dashboard" ? "Visão geral" : titleCase(tab)}</h1>
          <p>
            {tab === "dashboard"
              ? "Indicadores reais do período selecionado."
              : "Dados e ações conectados aos endpoints administrativos."}
          </p>
        </header>
        {success && <div className="success-toast">{success}</div>}
        {tab === "orders" && <OrderFiltersForm applied={orderFilters} loading={loading} onApply={filters => { setOrderFilters(filters); void load(filters); }} />}
        {error && <ErrorState message={error} retry={() => void load()} />}
        {tab === "dashboard" &&
          (loading ? (
            <Loading />
          ) : error ? null : (
            metrics && (
              <>
                <div className="form-row report-filter">
                  <div className="period-shortcuts" aria-label="Períodos rápidos">
                    <button type="button" onClick={() => selectPeriod(1)}>Hoje</button>
                    <button type="button" onClick={() => selectPeriod(7)}>7 dias</button>
                    <button type="button" onClick={() => selectPeriod("month")}>Este mês</button>
                    <button type="button" onClick={() => selectPeriod(30)}>30 dias</button>
                  </div>
                  <label>
                    Início
                    <input
                      type="date"
                      value={reportPeriod.inicio}
                      onChange={(e) =>
                        setReportPeriod({
                          ...reportPeriod,
                          inicio: e.target.value,
                        })
                      }
                    />
                  </label>
                  <label>
                    Fim
                    <input
                      type="date"
                      value={reportPeriod.fim}
                      onChange={(e) =>
                        setReportPeriod({
                          ...reportPeriod,
                          fim: e.target.value,
                        })
                      }
                    />
                  </label>
                  <button
                    className="button secondary"
                    onClick={() => void load()}
                  >
                    Atualizar
                  </button>
                </div>
                <div className="metric-grid">
                  {[
                    ["Faturamento", money(metrics.faturamento)],
                    ["Pedidos", String(metrics.pedidos)],
                    ["Ticket médio", money(metrics.ticketMedio)],
                    ["Produtos vendidos", String(metrics.produtosVendidos)],
                    ["Clientes no período", String(metrics.clientes)],
                    ["Aguardando retirada", String(metrics.aguardandoRetirada)],
                    ["Pedidos para entrega", String(metrics.pedidosEntrega)],
                  ].map(([label, value]) => (
                    <article key={label}>
                      <span>{label}</span>
                      <strong>{value}</strong>
                    </article>
                  ))}
                </div>
                <section className="chart-card"><h2>Estoque baixo — {(metrics.estoqueBaixo||[]).length} produtos</h2><p>Estoque atual de até {metrics.limiteEstoqueBaixo ?? 5} unidades, incluindo inativos. Independente do período de vendas.</p>{!(metrics.estoqueBaixo||[]).length?<p>Nenhum produto com estoque baixo.</p>:<div className="table-wrap"><table><thead><tr><th>SKU</th><th>Produto</th><th>Quantidade</th><th>Situação</th></tr></thead><tbody>{metrics.estoqueBaixo?.map(p=><tr key={p.produtoId}><td>{p.codigo}</td><td>{p.nome}</td><td>{p.quantidade}</td><td>{p.ativo?'Ativo':'Inativo'}</td></tr>)}</tbody></table></div>}</section>
                <section className="chart-card"><h2>Produtos mais vendidos no período</h2>{!(metrics.maisVendidos||[]).length?<p>Nenhuma venda aprovada no período.</p>:<div className="table-wrap"><table><thead><tr><th>Produto</th><th>Unidades</th><th>Valor dos itens</th></tr></thead><tbody>{metrics.maisVendidos?.map(p=><tr key={p.produtoId}><td>{p.nome}</td><td>{p.quantidade}</td><td>{money(p.faturamento||0)}</td></tr>)}</tbody></table></div>}</section>
                <div className="dashboard-grid">
                  <article className="chart-card">
                    <h2>Faturamento por período</h2>
                    <div className="bar-chart">
                      {(metrics.serieDiaria || []).map((item) => (
                        <div key={item.periodo}>
                          <i
                            style={{
                              height: `${Math.max(4, Math.min(100, (item.faturamento / Math.max(...(metrics.serieDiaria || []).map((v) => v.faturamento), 1)) * 100))}%`,
                            }}
                          />
                          <span>{item.periodo.slice(5)}</span>
                        </div>
                      ))}
                    </div>
                  </article>
                  <article className="chart-card">
                    <h2>Vendas por categoria</h2>
                    {Object.entries(metrics.categorias || {}).map(
                      ([nome, valor]) => (
                        <div className="category-meter" key={nome}>
                          <span>
                            {titleCase(nome)}
                            <b>{valor}</b>
                          </span>
                          <i>
                            <em
                              style={{
                                width: `${Math.min(100, Number(valor) * 10)}%`,
                              }}
                            />
                          </i>
                        </div>
                      ),
                    )}
                  </article>
                  <article className="chart-card status-summary">
                    <h2>Pedidos por status</h2>
                    {Object.entries(metrics.status || {}).map(
                      ([status, total]) => (
                        <p key={status}>
                          <b>{total}</b> {titleCase(status)}
                        </p>
                      ),
                    )}
                  </article>
                </div>
              </>
            )
          ))}
        {tab === "catalog" && !loading && !error && <CatalogAdmin items={registrations} reload={load} />}
        {tab !== "dashboard" && tab !== "catalog" &&
          !error &&
          (loading ? (
            <Loading />
          ) : (
            <>
              {tab === "products" && (
                <div className="admin-toolbar">
                  <label>
                    <Search />
                    <input
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                      placeholder={
                        tab === "products"
                          ? "Nome, código ou marca"
                          : "Número do pedido"
                      }
                    />
                  </label>
                  {tab === "products" && (
                    <><a className="button secondary" href="/api/v1/admin/produtos/importacao/modelo">Baixar modelo CSV</a><label className="button secondary">Importar CSV/XLSX<input hidden type="file" accept=".csv,.xlsx" onChange={async e=>{const file=e.target.files?.[0];if(!file)return;setImportFile(file);try{setImportPreview(await api.importProducts(file,true))}catch(error){setError(error instanceof Error?error.message:'Falha na pré-visualização.')}}}/></label><button
                      className="button primary"
                      onClick={() => openProductForm({ ...blankProduct })}
                    >
                      <Plus /> Novo produto
                    </button></>
                  )}
                </div>
              )}
              {tab==='products'&&importPreview&&<div className="review-alert"><strong>Pré-visualização: {importPreview.inseridos} novos, {importPreview.atualizados} atualizações, {importPreview.rejeitados} rejeitados.</strong>{importPreview.erros.map(error=><span key={error}>{error}</span>)}{importPreview.registros.length>0&&<div className="table-scroll"><table><thead><tr><th>Ação</th><th>SKU</th><th>Produto</th><th>Categoria</th><th>Preço</th><th>Estoque</th></tr></thead><tbody>{importPreview.registros.map(r=><tr key={r.codigo}><td>{r.acao}</td><td>{r.codigo}</td><td>{r.nome}</td><td>{r.categoria}</td><td>{money(r.preco)}</td><td>{r.estoque}</td></tr>)}</tbody></table></div>}<button className="button primary" disabled={!importFile||importPreview.rejeitados>0} onClick={()=>importFile&&void action(async()=>{await api.importProducts(importFile,false);setImportFile(null);setImportPreview(null);setImportHistory(await api.importHistory())},'Importação concluída e registrada no histórico.')}>Confirmar importação transacional</button></div>}
              {tab==='products'&&importHistory.length>0&&<section className="chart-card"><header><div><h2>Histórico de importações</h2><p>Arquivo, data, responsável e resultado das últimas importações confirmadas.</p></div></header><div className="table-scroll"><table><thead><tr><th>Data</th><th>Arquivo</th><th>Responsável</th><th>Resultado</th><th>Totais</th><th>Comprovante</th></tr></thead><tbody>{importHistory.map(h=><tr key={h.id}><td>{date(h.criadoEm)}</td><td><strong>{h.nomeArquivo}</strong><small>SHA-256: {h.hashArquivo.slice(0,12)}…</small></td><td>{h.responsavel}</td><td>{titleCase(h.resultado)}</td><td>{h.inseridos} inseridos · {h.atualizados} atualizados · {h.rejeitados} rejeitados</td><td><a className="text-link" href={api.importHistoryFileUrl(h.id)}>Baixar arquivo</a></td></tr>)}</tbody></table></div></section>}
              {tab === "users" && (
                <div className="admin-toolbar">
                  <span />
                  <button
                    className="button primary"
                    onClick={() =>
                      setCollaboratorForm({
                        nome: "NOVO",
                        email: "",
                        senha: "",
                        cpf: "",
                        telefone: "",
                      })
                    }
                  >
                    <UserPlus /> Novo colaborador
                  </button>
                </div>
              )}
              {tab === "coupons" && (
                <div className="admin-toolbar">
                  <span />
                  <button
                    className="button primary"
                    onClick={() =>
                      setCouponForm({
                        codigo: "",
                        descricao: "",
                        tipo: "PERCENTUAL",
                        desconto: 0,
                        minSubtotal: 0,
                        ativo: true,
                      })
                    }
                  >
                    <Plus /> Novo cupom
                  </button>
                </div>
              )}
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      {tab === "products" && (
                        <>
                          <th>Produto</th>
                          <th>Preço</th>
                          <th>Status</th>
                          <th>Ações</th>
                        </>
                      )}
                      {tab === "orders" && (
                        <>
                          <th>Pedido</th>
                          <th>Data</th>
                          <th>Total</th>
                          <th>Status</th>
                          <th>Detalhes</th>
                        </>
                      )}
                      {tab === "users" && (
                        <>
                          <th>Usuário</th>
                          <th>Perfil</th>
                          <th>Relatórios</th>
                          <th>Status</th>
                          <th>Ações</th>
                        </>
                      )}
                      {tab === "coupons" && (
                        <>
                          <th>Código</th>
                          <th>Desconto</th>
                          <th>Usos e pedidos</th>
                          <th>Status</th>
                          <th>Ações</th>
                        </>
                      )}
                    </tr>
                  </thead>
                  <tbody>
                    {tab === "products" &&
                      visibleProducts.map((p) => (
                        <tr key={p.id}>
                          <td>
                            <div className="admin-product-summary">
                              <img
                                src={p.imagemUrl || "/assets/imagens/logo-vita-fortis-brand.webp"}
                                onError={(event) => { event.currentTarget.src = "/assets/imagens/logo-vita-fortis-brand.webp"; }}
                                alt=""
                              />
                              <div>
                                <strong>{p.nome}</strong>
                                <div className="admin-product-badges">
                                  {p.preco == null && <span>Preço pendente</span>}
                                  {p.lancamento && <span>Novidade</span>}
                                  {(p.descontoPercentual || p.descontoValor) && <span>Oferta</span>}
                                </div>
                                <small>{p.codigo} • {p.marca} • {titleCase(p.subcategoria || p.categoria)}</small>
                              </div>
                            </div>
                          </td>
                          <td>
                            <strong>{p.preco == null ? "A definir" : money(p.precoFinal ?? p.preco)}</strong>
                            {p.preco != null && p.precoFinal != null && p.precoFinal < p.preco && (
                              <small>
                                <del>{money(p.preco)}</del> ·{" "}
                                {p.descontoPercentual
                                  ? `${p.descontoPercentual}%`
                                  : money(p.descontoValor)}
                              </small>
                            )}
                          </td>
                          <td>
                            <span
                              className={`status ${p.ativo ? "active" : "cancelado"}`}
                            >
                              {p.ativo ? "Ativo" : p.preco == null ? "Rascunho" : "Inativo"}
                            </span>
                          </td>
                          <td className="action-cell">
                            <button
                              title="Editar"
                              onClick={() => openProductForm({ ...p })}
                            >
                              <Pencil />
                            </button>
                            <button
                              title="Duplicar"
                              onClick={() =>
                                openProductForm({
                                  ...p,
                                  id: undefined,
                                  codigo: `${p.codigo}-COPIA`,
                                  nome: `${p.nome} — cópia`,
                                })
                              }
                            >
                              <Copy />
                            </button>
                            <button
                              className="text-action"
                              onClick={() => {
                                const value = prompt(
                                  "Desconto percentual (0 a 100):",
                                  String(p.descontoPercentual || 0),
                                );
                                if (value !== null)
                                  void action(
                                    () =>
                                      api.setDiscountPercent(
                                        p.id,
                                        Number(value),
                                      ),
                                    "Desconto percentual atualizado.",
                                  );
                              }}
                            >
                              %
                            </button>
                            <button
                              className="text-action"
                              onClick={() => {
                                const value = prompt(
                                  "Desconto em reais:",
                                  String(p.descontoValor || 0),
                                );
                                if (value !== null)
                                  void action(
                                    () =>
                                      api.setDiscountValue(p.id, Number(value)),
                                    "Desconto em valor atualizado.",
                                  );
                              }}
                            >
                              R$
                            </button>
                            {(p.descontoPercentual || p.descontoValor) && (
                              <button
                                className="text-action"
                                onClick={() =>
                                  void action(
                                    () => api.clearDiscount(p.id),
                                    "Desconto removido.",
                                  )
                                }
                              >
                                Sem desconto
                              </button>
                            )}
                            <button
                              className="text-action"
                              onClick={() =>
                                void action(
                                  () => api.setCommercialMetadata(p.id, {
                                    objetivos: p.objetivos,
                                    esportes: p.esportes,
                                    vegano: p.vegano,
                                    vegetariano: p.vegetariano,
                                    linhaClinica: p.linhaClinica,
                                    lancamento: !p.lancamento,
                                    subcategoria: p.subcategoria,
                                    avaliacaoMedia: p.avaliacaoMedia,
                                  }),
                                  p.lancamento ? "Produto removido das novidades." : "Produto marcado como novidade.",
                                )
                              }
                            >
                              {p.lancamento ? "Remover novidade" : "Marcar novidade"}
                            </button>
                            <button
                              className="text-action"
                              onClick={() =>
                                confirm(
                                  `${p.ativo ? "Desativar" : "Ativar"} ${p.nome}?`,
                                ) &&
                                void action(() =>
                                  api.setProductActive(p.id, !p.ativo),
                                )
                              }
                            >
                              {p.ativo ? "Desativar" : "Ativar"}
                            </button>
                            <button className="text-action danger" onClick={()=>confirm(`Arquivar ${p.nome}? O produto sairá do catálogo, mas o histórico será preservado.`)&&void action(()=>api.archiveProduct(p.id),'Produto arquivado com segurança.')}>Excluir/arquivar</button>
                          </td>
                        </tr>
                      ))}
                    {tab === "orders" && orders.length === 0 && <tr><td colSpan={5}><p role="status">Nenhum pedido encontrado para os filtros aplicados.</p></td></tr>}
                    {tab === "orders" &&
                      orders
                        .map((o) => (
                          <tr key={o.id}>
                            <td>
                              <strong>#{o.id}</strong>
                              <small>
                                {o.itens.length} item(ns) • pagamento{" "}
                                {titleCase(o.statusPagamento)}
                              </small>
                            </td>
                            <td>{date(o.dataPedido)}</td>
                            <td>{money(o.total)}</td>
                            <td>
                              {o.statusPagamento === "PENDENTE" ? (
                                <div className="payment-actions">
                                  <button
                                    onClick={() =>
                                      confirm(
                                        "Aprovar o pagamento simulado?",
                                      ) &&
                                      void action(
                                        () => api.approvePayment(o.id),
                                        "Pagamento aprovado.",
                                      )
                                    }
                                  >
                                    Aprovar
                                  </button>
                                  <button
                                    className="danger"
                                    onClick={() =>
                                      confirm(
                                        "Recusar o pagamento e cancelar o pedido?",
                                      ) &&
                                      void action(
                                        () => api.rejectPayment(o.id),
                                        "Pagamento recusado e pedido cancelado.",
                                      )
                                    }
                                  >
                                    Recusar
                                  </button>
                                </div>
                              ) : (
                                <select
                                  value={o.status}
                                  onChange={(e) =>
                                    confirm("Confirmar alteração do status?") &&
                                    void action(() =>
                                      api.setOrderStatus(o.id, e.target.value),
                                    )
                                  }
                                >
                                  {[
                                    "PAGAMENTO_APROVADO",
                                    "EM_SEPARACAO",
                                    "DISPONIVEL_RETIRADA",
                                    "ENVIADO",
                                    "ENTREGUE",
                                    "CANCELADO",
                                  ].map((s) => (
                                    <option key={s}>{s}</option>
                                  ))}
                                </select>
                              )}
                            </td>
                            <td>
                              <button
                                className="icon-button"
                                onClick={() => setOrderOpen(o)}
                              >
                                <Eye />
                              </button>
                            </td>
                          </tr>
                        ))}
                    {tab === "users" &&
                      users.map((u) => (
                        <tr key={u.id}>
                          <td>
                            <strong>{u.nome}</strong>
                            <small>{u.email}</small>
                          </td>
                          <td>
                            <select
                              value={u.tipoUsuario}
                              onChange={(e) =>
                                confirm("Alterar permissão deste usuário?") &&
                                void action(() =>
                                  api.setUserRole(u.id, e.target.value),
                                )
                              }
                            >
                              {["CLIENTE", "COLABORADOR", "ADMIN"].map((r) => (
                                <option key={r}>{r}</option>
                              ))}
                            </select>
                          </td>
                          <td>
                            {u.tipoUsuario === "COLABORADOR" ? (
                              <label className="inline-check">
                                <input
                                  type="checkbox"
                                  checked={Boolean(u.permissaoRelatorios)}
                                  onChange={(e) =>
                                    void action(
                                      () =>
                                        api.setReportPermission(
                                          u.id,
                                          e.target.checked,
                                        ),
                                      "Permissão atualizada.",
                                    )
                                  }
                                />{" "}
                                Relatórios
                              </label>
                            ) : (
                              "—"
                            )}
                          </td>
                          <td>
                            <span
                              className={`status ${u.ativo ? "active" : "cancelado"}`}
                            >
                              {u.ativo ? "Ativo" : "Inativo"}
                            </span>
                          </td>
                          <td className="action-cell">
                            <button
                              className="text-action"
                              onClick={() =>
                                confirm("Confirmar alteração de acesso?") &&
                                void action(() =>
                                  api.setUserActive(u.id, !u.ativo),
                                )
                              }
                            >
                              {u.ativo ? "Desativar" : "Ativar"}
                            </button>
                            {u.tipoUsuario === "COLABORADOR" && (
                              <button
                                title="Movimentar cashback"
                                onClick={() =>
                                  setCashbackForm({
                                    id: u.id,
                                    nome: u.nome,
                                    tipo: "BAIXA",
                                    valor: 0,
                                    justificativa: "",
                                  })
                                }
                              >
                                <WalletCards />
                              </button>
                            )}
                          </td>
                        </tr>
                      ))}
                    {tab === "coupons" &&
                      coupons.map((c) => (
                        <tr key={c.id}>
                          <td>
                            <strong>{c.codigo}</strong>
                            <small>
                              {c.descricao}
                              {c.colaboradorNome && ` • ${c.colaboradorNome}`}
                            </small>
                          </td>
                          <td>
                            {c.tipo === "PERCENTUAL"
                              ? `${c.desconto}%`
                              : money(c.desconto)}
                          </td>
                          <td>
                            <strong>{c.quantidadeUsos || 0} uso(s) — {money(c.valorTotalConcedido || 0)} concedidos</strong>
                            <small>
                              {c.pedidoIds?.length
                                ? `Pedidos: ${c.pedidoIds.map((id) => `#${id}`).join(", ")}`
                                : "Nenhum pedido"}
                            </small>
                            <small>
                              {c.limiteUso ? `Limite: ${c.limiteUso} uso(s)` : "Sem limite de uso"}
                              {` • ${c.dataInicio ? `início ${date(c.dataInicio)}` : "início imediato"}`}
                              {` • ${c.dataVencimento ? `vence ${date(c.dataVencimento)}` : "sem vencimento"}`}
                            </small>
                          </td>
                          <td>
                            <span
                              className={`status ${c.ativo ? "active" : "cancelado"}`}
                            >
                              {c.ativo ? "Ativo" : "Inativo"}
                            </span>
                          </td>
                          <td className="action-cell">
                            <button
                              title="Editar cupom"
                              onClick={() => setCouponForm({ ...c })}
                            >
                              <Pencil />
                            </button>
                            <button
                              className="text-action"
                              onClick={() =>
                                confirm("Confirmar alteração do cupom?") &&
                                void action(() =>
                                  api.setCouponActive(c.id, !c.ativo),
                                )
                              }
                            >
                              {c.ativo ? "Desativar" : "Ativar"}
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </>
          ))}
        {tab === "reviews" && !loading && !error && (
          <div className="review-admin-list">
            {!reviews.length ? (
              <p className="empty-inline">Nenhuma avaliação cadastrada.</p>
            ) : (
              reviews.map((r) => (
                <article className="chart-card" key={r.id}>
                  <header>
                    <div>
                      <strong>
                        {r.usuarioNome || `Usuário #${r.usuarioId}`}
                      </strong>
                      <small>
                        Produto #{r.produtoId} · {date(r.criadoEm)}
                      </small>
                    </div>
                    <span
                      className={`status ${r.aprovado ? "active" : "cancelado"}`}
                    >
                      {r.aprovado ? "Publicada" : "Oculta"}
                    </span>
                  </header>
                  <p>
                    {"★".repeat(r.nota)}
                    {"☆".repeat(5 - r.nota)} —{" "}
                    {r.comentario || "Sem comentário"}
                  </p>
                  <button
                    className="button secondary"
                    onClick={() =>
                      void action(
                        () => api.moderateReview(r.id, !r.aprovado),
                        "Moderação atualizada.",
                      )
                    }
                  >
                    {r.aprovado ? "Ocultar" : "Aprovar"}
                  </button>
                </article>
              ))
            )}
          </div>
        )}
        {tab === "privacy" && <PrivacyAdmin />}
        {tab === "reports" && (
          <section className="chart-card report-panel">
            <span className="eyebrow">Central de exportação</span><h2>Relatórios administrativos</h2><p>Escolha o período, o formato e o conteúdo que deseja baixar.</p>
            <div className="form-row report-period">
              <label>
                Início
                <input
                  type="date"
                  value={reportPeriod.inicio}
                  onChange={(e) =>
                    setReportPeriod({ ...reportPeriod, inicio: e.target.value })
                  }
                />
              </label>
              <label>
                Fim
                <input
                  type="date"
                  value={reportPeriod.fim}
                  onChange={(e) =>
                    setReportPeriod({ ...reportPeriod, fim: e.target.value })
                  }
                />
              </label>
            </div>
            <div className="form-grid">
              <label>Conteúdo<select value={reportType} onChange={e=>{setReportType(e.target.value);setReportFilters({})}}>{["VENDAS","PEDIDOS","PRODUTOS","CLIENTES","CUPONS","CASHBACK"].map(tipo=><option key={tipo} value={tipo}>{titleCase(tipo)}</option>)}</select></label>
              <label>Buscar<input value={reportFilters.busca||''} maxLength={120} placeholder={['VENDAS','PEDIDOS'].includes(reportType)?'Pedido, nome ou e-mail do cliente':'Nome, código ou e-mail'} onChange={e=>filterReport('busca',e.target.value)}/></label>
              {['VENDAS','PEDIDOS'].includes(reportType)&&<>
                <label>Status do pedido<select value={reportFilters.status||''} onChange={e=>filterReport('status',e.target.value)}><option value="">Todos</option>{['PENDENTE','PAGAMENTO_APROVADO','EM_SEPARACAO','DISPONIVEL_RETIRADA','ENVIADO','ENTREGUE','CANCELADO'].map(v=><option key={v}>{v}</option>)}</select></label>
                <label>Recebimento<select value={reportFilters.recebimento||''} onChange={e=>filterReport('recebimento',e.target.value)}><option value="">Todos</option><option value="RETIRADA">Retirada</option><option value="ENTREGA">Entrega</option></select></label>
                <label>Pagamento<select value={reportFilters.pagamento||''} onChange={e=>filterReport('pagamento',e.target.value)}><option value="">Todos</option>{['PIX','CARTAO','BOLETO'].map(v=><option key={v}>{v}</option>)}</select></label>
              </>}
              {['PRODUTOS','CLIENTES','CUPONS'].includes(reportType)&&<label>Situação<select value={reportFilters.ativo||''} onChange={e=>filterReport('ativo',e.target.value)}><option value="">Todas</option><option value="true">Ativo</option><option value="false">Inativo</option></select></label>}
              {reportType==='PRODUTOS'&&<>
                <label>Categoria<select value={reportFilters.categoria||''} onChange={e=>filterReport('categoria',e.target.value)}><option value="">Todas</option>{registrations.filter(c=>c.tipo==='CATEGORIA').map(c=><option key={c.id} value={c.codigo}>{c.nome}</option>)}</select></label>
                <label>Estoque até<input type="number" min="0" step="1" value={reportFilters.estoqueMax||''} onChange={e=>filterReport('estoqueMax',e.target.value)} placeholder="Ex.: 5"/></label>
              </>}
              {['CUPONS','CASHBACK'].includes(reportType)&&<label>Colaborador<select value={reportFilters.colaboradorId||''} onChange={e=>filterReport('colaboradorId',e.target.value)}><option value="">Todos</option>{users.filter(u=>u.tipoUsuario==='COLABORADOR').map(u=><option key={u.id} value={u.id}>{u.nome}</option>)}</select></label>}
              <label>Formato<select value={reportFormat} onChange={e=>setReportFormat(e.target.value as 'csv'|'pdf'|'xlsx')}><option value="csv">CSV</option><option value="pdf">PDF</option><option value="xlsx">XLSX</option></select></label>
            </div>
            <p>{['PRODUTOS','CLIENTES'].includes(reportType)?'Cadastro atual: o período não se aplica a este relatório.':'O período inclui as datas inicial e final. Vendas e usos de cupons consideram somente pagamentos aprovados; cashback considera a data da movimentação.'}</p>
            <div className="report-actions"><button className="button primary" disabled={reportDownloading||!reportPeriod.inicio||!reportPeriod.fim||reportPeriod.fim<reportPeriod.inicio} onClick={async()=>{setReportDownloading(true);setError('');setSuccess('');try{await api.downloadReport(reportType,reportPeriod.inicio,reportPeriod.fim,reportFormat,reportFilters);setSuccess('Relatório gerado.')}catch(e){setError(e instanceof Error?e.message:'Falha ao gerar relatório.')}finally{setReportDownloading(false)}}}><Download/>{reportDownloading?'Gerando…':'Baixar relatório'}</button></div>
            {reportPeriod.fim<reportPeriod.inicio&&<p role="alert">A data final deve ser igual ou posterior à inicial.</p>}

          </section>
        )}
      </section>
      {productForm && (
        <div className="modal-backdrop">
          <form className="admin-modal product-editor" onSubmit={submitProduct} aria-busy={productSaving || imageUploading}>
            <header>
              <div>
                <span className="eyebrow">Cadastro</span>
                <h2>{productForm.id ? "Editar produto" : "Novo produto"}</h2>
              </div>
              <button type="button" disabled={imageUploading || productSaving} aria-label="Fechar cadastro de produto" onClick={() => openProductForm(null)}>
                <X />
              </button>
            </header>
            <fieldset className="form-grid product-editor-fields" disabled={productSaving}>
              {([ ["codigo", "Código"], ["nome", "Nome"], ["preco", "Preço original"], ["descontoPercentual", "Desconto (%)"], ["descontoValor", "Desconto (R$)"], ["avaliacaoMedia", "Avaliação média"] ] as const).map(([key, label]) => (
                <label key={key}>
                  {label}
                  <input
                    required={[
                      "codigo",
                      "nome",
                      "preco",
                      "categoria", "marca",
                    ].includes(key)}
                    type={
                      ["preco", "descontoPercentual", "descontoValor", "avaliacaoMedia"].includes(
                        key,
                      )
                        ? "number"
                        : "text"
                    }
                    step={
                      ["preco", "avaliacaoMedia"].includes(key)
                        ? ".01"
                        : undefined
                    }
                    value={String(productForm[key] ?? "")}
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        [key]: [
                          "preco",
                          "descontoPercentual",
                          "descontoValor",
                          "avaliacaoMedia",
                        ].includes(key)
                          ? Number(e.target.value)
                          : e.target.value,
                      })
                    }
                  />
                </label>
              ))}
              <label>Marca<input required list="product-brands" value={productForm.marca || ""} placeholder="Selecione ou digite a marca" onChange={e => setProductForm({ ...productForm, marca: e.target.value })} /><datalist id="product-brands">{Array.from(new Set(products.map(p => p.marca).filter(Boolean))).sort().map(value => <option key={value} value={value} />)}</datalist></label>
              <label>Categoria<select required value={productForm.categoria||""} onChange={e=>setProductForm({...productForm,categoria:e.target.value})}><option value="">Selecione</option>{registrations.filter(c=>c.tipo==='CATEGORIA'&&(c.ativo||c.codigo===productForm.categoria)).map(c=><option key={c.id} value={c.codigo}>{c.nome}{!c.ativo?' (inativa)':''}</option>)}</select></label>
              {registrations.filter(c=>c.tipo==='ATRIBUTO'&&(c.ativo||productForm.atributos?.[c.codigo])).map(c=><label key={c.id}>{c.nome}<input maxLength={255} disabled={!c.ativo} value={productForm.atributos?.[c.codigo]||''} onChange={e=>{const atributos={...productForm.atributos};if(e.target.value.trim())atributos[c.codigo]=e.target.value;else delete atributos[c.codigo];setProductForm({...productForm,atributos})}} /></label>)}
              <label>Subcategoria<input list="product-subcategories" value={productForm.subcategoria || ""} placeholder="Selecione ou digite a subcategoria" onChange={e => setProductForm({ ...productForm, subcategoria: e.target.value })} /><datalist id="product-subcategories">{Array.from(new Set(products.map(p => p.subcategoria).filter(Boolean))).sort().map(value => <option key={value} value={value} />)}</datalist></label>
              <label>Unidade ou peso<input list="product-units" value={productForm.unidade || ""} placeholder="Ex.: 300g ou 60 cápsulas" onChange={e => setProductForm({ ...productForm, unidade: e.target.value })} /><datalist id="product-units">{Array.from(new Set(products.map(p => p.unidade).filter(Boolean))).sort().map(value => <option key={value} value={value} />)}</datalist></label>
              <div className="full image-upload-field">
                <label htmlFor="product-image">Imagem do produto</label>
                <div className="image-upload-box">
                  {productForm.imagemUrl ? <img src={productForm.imagemUrl} alt="Pré-visualização do produto" /> : <ImagePlus aria-hidden="true" />}
                  <div><strong>{imageUploading ? 'Enviando imagem…' : 'Clique para selecionar a imagem'}</strong><small id="product-image-help">JPG, PNG ou WebP · até 5 MB · recomendado 1200 × 1200 px</small></div>
                  <input id="product-image" type="file" accept="image/jpeg,image/png,image/webp" aria-describedby={`product-image-help${imageError ? ' product-image-error' : ''}`} aria-invalid={Boolean(imageError)} disabled={imageUploading || productSaving} onChange={e => { const file = e.target.files?.[0]; e.target.value = ''; if (file) void uploadProductImage(file) }} />
                </div>
                {imageError && <p id="product-image-error" className="commerce-feedback error" role="alert">{imageError}</p>}
                <p className="commerce-feedback" role="status">{imageUploading ? "Enviando imagem. Aguarde para salvar o produto." : imageStatus}</p>
              </div>
              <label className="full">
                Descrição
                <textarea
                  required
                  value={productForm.descricao || ""}
                  onChange={(e) =>
                    setProductForm({
                      ...productForm,
                      descricao: e.target.value,
                    })
                  }
                />
              </label>
              <fieldset className="full objective-picker"><legend>Objetivos</legend>{[["EMAGRECIMENTO","Emagrecimento"],["GANHO_DE_MASSA","Ganho de massa"],["PERFORMANCE","Performance"],["SAUDE_E_BEM_ESTAR","Saúde e bem-estar"]].map(([value,label])=><label className="check-row" key={value}><input type="checkbox" checked={(productForm.objetivos||[]).includes(value)} onChange={e=>setProductForm({...productForm,objetivos:e.target.checked?[...(productForm.objetivos||[]),value]:(productForm.objetivos||[]).filter(item=>item!==value)})}/>{label}</label>)}</fieldset>
              <fieldset className="full objective-picker"><legend>Esportes</legend>{Array.from(new Set([...(productForm.esportes || []), "ACADEMIA", "ARTES_MARCIAIS", "ATLETISMO", "BASQUETE", "CICLISMO", "CORRIDA", "CROSS_TRAINING", "FUTEBOL", "NATACAO", "OUTROS"])).map(value => <label className="check-row" key={value}><input type="checkbox" checked={(productForm.esportes || []).includes(value)} onChange={e => setProductForm({ ...productForm, esportes: e.target.checked ? [...(productForm.esportes || []), value] : (productForm.esportes || []).filter(item => item !== value) })} />{titleCase(value)}</label>)}</fieldset>
              {(
                [
                  ["vegano", "Vegano"],
                  ["vegetariano", "Vegetariano"],
                  ["linhaClinica", "Linha clínica"],
                  ["lancamento", "Marcar como novidade"],
                  ["destaque", "Exibir em destaque"],
                  ["oferta", "Produto em oferta"],
                  ["kit", "Este item é um kit"],
                ] as const
              ).map(([key, label]) => (
                <label className="check-row" key={key}>
                  <input
                    type="checkbox"
                    checked={Boolean(productForm[key])}
                    onChange={(e) =>
                      setProductForm({
                        ...productForm,
                        [key]: e.target.checked,
                      })
                    }
                  />{" "}
                  {label}
                </label>
              ))}
            </fieldset>
            {productError && <p className="commerce-feedback error" role="alert">{productError}</p>}
            <footer>
              <button
                type="button"
                className="button secondary"
                disabled={imageUploading || productSaving}
                onClick={() => openProductForm(null)}
              >
                Cancelar
              </button>
              <button className="button primary" disabled={imageUploading || productSaving}>{productSaving ? "Salvando produto…" : imageUploading ? "Aguarde o envio da imagem…" : "Salvar produto"}</button>
            </footer>
          </form>
        </div>
      )}
      {couponForm && (
        <div className="modal-backdrop">
          <form className="admin-modal small" onSubmit={submitCoupon}>
            {couponError && <p role="alert" className="form-error">{couponError}</p>}
            <header>
              <h2>{couponForm.id ? "Editar cupom" : "Novo cupom"}</h2>
              <button type="button" onClick={() => setCouponForm(null)}>
                <X />
              </button>
            </header>
            <div className="form-grid">
              <label>
                Código
                <input
                  required
                  value={couponForm.codigo || ""}
                  onChange={(e) =>
                    setCouponForm({
                      ...couponForm,
                      codigo: e.target.value.toUpperCase(),
                    })
                  }
                />
              </label>
              <label>
                Tipo
                <select
                  value={couponForm.tipo}
                  onChange={(e) =>
                    setCouponForm({
                      ...couponForm,
                      tipo: e.target.value as Coupon["tipo"],
                    })
                  }
                >
                  <option>PERCENTUAL</option>
                  <option>FIXO</option>
                </select>
              </label>
              <label>
                {couponForm.tipo === "PERCENTUAL" ? "Desconto (%)" : "Desconto (R$)"}
                <input
                  required
                  type="number"
                  min="0.01"
                  max={couponForm.tipo === "PERCENTUAL" ? 100 : undefined}
                  step=".01"
                  value={couponForm.desconto || 0}
                  onChange={(e) =>
                    setCouponForm({
                      ...couponForm,
                      desconto: Number(e.target.value),
                    })
                  }
                />
              </label>
              <label>
                Subtotal mínimo
                <input
                  min="0"
                  type="number"
                  step=".01"
                  value={couponForm.minSubtotal || 0}
                  onChange={(e) =>
                    setCouponForm({
                      ...couponForm,
                      minSubtotal: Number(e.target.value),
                    })
                  }
                />
              </label>
              <label>Início da validade<input type="datetime-local" value={couponForm.dataInicio?.slice(0,16)||''} onChange={e=>setCouponForm({...couponForm,dataInicio:e.target.value||null})} /></label>
              <label>Fim da validade<input type="datetime-local" min={couponForm.dataInicio?.slice(0,16)||undefined} value={couponForm.dataVencimento?.slice(0,16)||''} onChange={e=>setCouponForm({...couponForm,dataVencimento:e.target.value||null})} /></label>
              <label>Limite de uso total<input type="number" min="1" step="1" placeholder="Sem limite" value={couponForm.limiteUso??''} onChange={e=>setCouponForm({...couponForm,limiteUso:e.target.value?Number(e.target.value):null})} /><small>Cada pedido criado consome um uso, inclusive se cancelado. Validade no horário de Brasília. Deixe vazio para não limitar.</small></label>
              <label>
                Colaborador
                <select
                  value={couponForm.colaboradorId || ""}
                  onChange={(e) =>
                    setCouponForm({
                      ...couponForm,
                      colaboradorId: e.target.value
                        ? Number(e.target.value)
                        : undefined,
                      percentualCashback: e.target.value
                        ? couponForm.percentualCashback || 5
                        : undefined,
                    })
                  }
                >
                  <option value="">Sem vínculo</option>
                  {users
                    .filter((u) => u.tipoUsuario === "COLABORADOR" && u.ativo)
                    .map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.nome} — {u.email}
                      </option>
                    ))}
                </select>
              </label>
              <label>
                Cashback do colaborador (%)
                <input
                  type="number"
                  min="0"
                  max="100"
                  step=".01"
                  disabled={!couponForm.colaboradorId}
                  value={couponForm.percentualCashback || 0}
                  onChange={(e) =>
                    setCouponForm({
                      ...couponForm,
                      percentualCashback: Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="full">
                Descrição
                <input
                  value={couponForm.descricao || ""}
                  onChange={(e) =>
                    setCouponForm({ ...couponForm, descricao: e.target.value })
                  }
                />
              </label>
            </div>
            <footer>
              <button
                type="button"
                className="button secondary"
                onClick={() => setCouponForm(null)}
              >
                Cancelar
              </button>
              <button className="button primary" disabled={couponSaving}>{couponSaving?"Salvando…":"Salvar cupom"}</button>
            </footer>
          </form>
        </div>
      )}
      {collaboratorForm.nome && (
        <div className="modal-backdrop">
          <form
            className="admin-modal small"
            onSubmit={(e) => {
              e.preventDefault();
              void action(
                () =>
                  api.createCollaborator({
                    ...collaboratorForm,
                    nome:
                      collaboratorForm.nome === "NOVO"
                        ? ""
                        : collaboratorForm.nome,
                  }),
                "Colaborador cadastrado.",
              );
              setCollaboratorForm({
                nome: "",
                email: "",
                senha: "",
                cpf: "",
                telefone: "",
              });
            }}
          >
            <header>
              <h2>Novo colaborador</h2>
              <button
                type="button"
                onClick={() =>
                  setCollaboratorForm({
                    nome: "",
                    email: "",
                    senha: "",
                    cpf: "",
                    telefone: "",
                  })
                }
              >
                <X />
              </button>
            </header>
            <div className="form-grid">
              {(["nome", "email", "cpf", "telefone", "senha"] as const).map(
                (key) => (
                  <label key={key}>
                    {titleCase(key)}
                    <input
                      required={key !== "telefone"}
                      type={
                        key === "senha"
                          ? "password"
                          : key === "email"
                            ? "email"
                            : "text"
                      }
                      minLength={key === "senha" ? 8 : undefined}
                      value={
                        collaboratorForm[key] === "NOVO"
                          ? ""
                          : collaboratorForm[key]
                      }
                      onChange={(e) =>
                        setCollaboratorForm({
                          ...collaboratorForm,
                          [key]: e.target.value,
                        })
                      }
                    />
                  </label>
                ),
              )}
            </div>
            <footer>
              <button
                type="button"
                className="button secondary"
                onClick={() =>
                  setCollaboratorForm({
                    nome: "",
                    email: "",
                    senha: "",
                    cpf: "",
                    telefone: "",
                  })
                }
              >
                Cancelar
              </button>
              <button className="button primary">Cadastrar colaborador</button>
            </footer>
          </form>
        </div>
      )}
      {cashbackForm && (
        <div className="modal-backdrop">
          <form
            className="admin-modal small"
            onSubmit={(e) => {
              e.preventDefault();
              const f = cashbackForm;
              void action(
                () =>
                  f.tipo === "AJUSTE"
                    ? api.adjustCashback(f.id, f.valor, f.justificativa)
                    : api.withdrawCashback(f.id, f.valor, f.justificativa),
                "Movimentação de cashback registrada.",
              );
              setCashbackForm(null);
            }}
          >
            <header>
              <h2>Cashback · {cashbackForm.nome}</h2>
              <button type="button" onClick={() => setCashbackForm(null)}>
                <X />
              </button>
            </header>
            <div className="form-grid">
              <label>
                Operação
                <select
                  value={cashbackForm.tipo}
                  onChange={(e) =>
                    setCashbackForm({
                      ...cashbackForm,
                      tipo: e.target.value as "AJUSTE" | "BAIXA",
                    })
                  }
                >
                  <option value="BAIXA">Baixa/pagamento</option>
                  <option value="AJUSTE">Definir novo saldo</option>
                </select>
              </label>
              <label>
                {cashbackForm.tipo === "AJUSTE"
                  ? "Novo saldo"
                  : "Valor da baixa"}
                <input
                  type="number"
                  min="0"
                  step=".01"
                  required
                  value={cashbackForm.valor}
                  onChange={(e) =>
                    setCashbackForm({
                      ...cashbackForm,
                      valor: Number(e.target.value),
                    })
                  }
                />
              </label>
              <label className="full">
                Justificativa
                <textarea
                  required
                  minLength={3}
                  value={cashbackForm.justificativa}
                  onChange={(e) =>
                    setCashbackForm({
                      ...cashbackForm,
                      justificativa: e.target.value,
                    })
                  }
                />
              </label>
            </div>
            <footer>
              <button
                type="button"
                className="button secondary"
                onClick={() => setCashbackForm(null)}
              >
                Cancelar
              </button>
              <button className="button primary">Confirmar movimentação</button>
            </footer>
          </form>
        </div>
      )}
      {orderOpen && (
        <div className="modal-backdrop">
          <div className="admin-modal small">
            <header>
              <h2>Pedido #{orderOpen.id}</h2>
              <button onClick={() => setOrderOpen(null)}>
                <X />
              </button>
            </header>
            {orderOpen.itens.map((item) => (
              <div className="order-detail-row" key={item.id}>
                <img src={item.produtoImagemUrl||"/assets/imagens/logo-vita-fortis-brand.webp"} alt="" onError={e => { const img = e.currentTarget; const fallback = "/assets/imagens/logo-vita-fortis-brand.webp"; if (!img.src.endsWith(fallback)) img.src = fallback; }} />
                <span>
                  <b>{item.produtoNome}</b><small>{item.quantidade} unidade(s) · {money(item.precoUnitario)} cada</small>
                </span>
                <b>{money(item.subtotal)}</b>
              </div>
            ))}
            <footer>
              <strong>Total: {money(orderOpen.total)}</strong>
            </footer>
          </div>
        </div>
      )}
    </div>
  );
}
