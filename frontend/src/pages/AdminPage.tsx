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
import {
  isAdmin,
  type AdminMetrics,
  type Coupon,
  type Order,
  type Product,
  type Review,
  type User,
} from "../types/api";
import { date, money, titleCase } from "../utils/format";
type Tab =
  | "dashboard"
  | "products"
  | "orders"
  | "users"
  | "coupons"
  | "reviews"
  | "reports";
const blankProduct: Partial<Product> = {
  codigo: "",
  nome: "",
  descricao: "",
  marca: "",
  unidade: "",
  preco: 0,
  quantidadeEstoque: 0,
  categoria: "PROTEINAS",
  imagemUrl: "",
  ativo: true,
  lancamento: false,
  valorDesconto: 0,
};
export function AdminPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState<Tab>("dashboard");
  const [products, setProducts] = useState<Product[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [metrics, setMetrics] = useState<AdminMetrics | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [search, setSearch] = useState("");
  const [productForm, setProductForm] = useState<Partial<Product> | null>(null);
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
  const load = async () => {
    if (tab === "dashboard" && reportPeriod.fim < reportPeriod.inicio) {
      setError("A data final deve ser igual ou posterior à data inicial.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      if (tab === "dashboard")
        setMetrics(await api.metrics(reportPeriod.inicio, reportPeriod.fim));
      if (tab === "products")
        setProducts(
          (
            await api.adminProducts({
              tamanho: 500,
              busca: search || undefined,
            })
          ).content,
        );
      if (tab === "orders") setOrders(await api.adminOrders());
      if (tab === "users") setUsers(await api.adminUsers());
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
  const submitProduct = (e: React.FormEvent) => {
    e.preventDefault();
    if (!productForm) return;
    const form = productForm;
    void action(
      async () => {
        const saved = await api.saveProduct(form, form.id);
        await api.setCommercialMetadata(saved.id, {
          objetivos: form.objetivos,
          esportes: form.esportes,
          vegano: form.vegano,
          vegetariano: form.vegetariano,
          linhaClinica: form.linhaClinica,
          lancamento: form.lancamento,
          subcategoria: form.subcategoria,
          avaliacaoMedia: form.avaliacaoMedia,
        });
        if (Number(form.descontoPercentual) > 0)
          await api.setDiscountPercent(
            saved.id,
            Number(form.descontoPercentual),
          );
        else if (Number(form.descontoValor) > 0)
          await api.setDiscountValue(saved.id, Number(form.descontoValor));
        else if (form.id) await api.clearDiscount(saved.id);
      },
      form.id ? "Produto atualizado." : "Produto criado.",
    );
    setProductForm(null);
  };
  const submitCoupon = (e: React.FormEvent) => {
    e.preventDefault();
    if (!couponForm) return;
    void action(
      () => api.saveCoupon(couponForm, couponForm.id),
      "Cupom salvo.",
    );
    setCouponForm(null);
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
            ["orders", PackageCheck, "Pedidos"],
            ["users", Users, "Usuários"],
            ["coupons", TicketPercent, "Cupons"],
            ["reviews", MessageSquare, "Avaliações"],
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
        {error && <ErrorState message={error} retry={load} />}
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
        {tab !== "dashboard" &&
          !error &&
          (loading ? (
            <Loading />
          ) : (
            <>
              {(tab === "products" || tab === "orders") && (
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
                    <button
                      className="button primary"
                      onClick={() => setProductForm({ ...blankProduct })}
                    >
                      <Plus /> Novo produto
                    </button>
                  )}
                </div>
              )}
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
                          <th>Estoque</th>
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
                            <input
                              className="stock-input"
                              type="number"
                              min="0"
                              defaultValue={p.quantidadeEstoque}
                              onBlur={(e) => {
                                const quantidade = Number(e.target.value);
                                if (quantidade !== p.quantidadeEstoque) {
                                  const motivo = prompt(
                                    "Informe o motivo deste ajuste de estoque:",
                                  );
                                  if (motivo)
                                    void action(
                                      () =>
                                        api.setStock(p.id, quantidade, motivo),
                                      "Estoque atualizado e auditado.",
                                    );
                                  else
                                    e.target.value = String(
                                      p.quantidadeEstoque,
                                    );
                                }
                              }}
                            />
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
                              onClick={() => setProductForm({ ...p })}
                            >
                              <Pencil />
                            </button>
                            <button
                              title="Duplicar"
                              onClick={() =>
                                setProductForm({
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
                          </td>
                        </tr>
                      ))}
                    {tab === "orders" &&
                      orders
                        .filter((o) => !search || String(o.id).includes(search))
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
                            <strong>{c.quantidadeUsos || 0} uso(s)</strong>
                            <small>
                              {c.pedidoIds?.length
                                ? `Pedidos: ${c.pedidoIds.map((id) => `#${id}`).join(", ")}`
                                : "Nenhum pedido"}
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
        {tab === "reports" && (
          <section className="chart-card report-panel">
            <h2>Exportar relatórios CSV</h2>
            <div className="form-row">
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
            <div className="report-actions">
              {["PEDIDOS", "PRODUTOS", "CLIENTES", "CUPONS", "CASHBACK"].map(
                (tipo) => (
                  <button
                    className="button secondary"
                    key={tipo}
                    onClick={() =>
                      void action(
                        () =>
                          api.downloadReport(
                            tipo,
                            reportPeriod.inicio,
                            reportPeriod.fim,
                          ),
                        `Relatório ${titleCase(tipo)} gerado.`,
                      )
                    }
                  >
                    <Download /> {titleCase(tipo)}
                  </button>
                ),
              )}
            </div>
          </section>
        )}
      </section>
      {productForm && (
        <div className="modal-backdrop">
          <form className="admin-modal" onSubmit={submitProduct}>
            <header>
              <div>
                <span className="eyebrow">Cadastro</span>
                <h2>{productForm.id ? "Editar produto" : "Novo produto"}</h2>
              </div>
              <button type="button" onClick={() => setProductForm(null)}>
                <X />
              </button>
            </header>
            <div className="form-grid">
              {(
                [
                  ["codigo", "Código"],
                  ["nome", "Nome"],
                  ["marca", "Marca"],
                  ["unidade", "Unidade ou peso"],
                  ["preco", "Preço original"],
                  ["quantidadeEstoque", "Estoque"],
                  ["categoria", "Categoria"],
                  ["imagemUrl", "URL da imagem"],
                  ["subcategoria", "Subcategoria"],
                  ["avaliacaoMedia", "Avaliação média"],
                ] as const
              ).map(([key, label]) => (
                <label key={key}>
                  {label}
                  <input
                    required={[
                      "codigo",
                      "nome",
                      "preco",
                      "quantidadeEstoque",
                      "categoria",
                    ].includes(key)}
                    type={
                      ["preco", "quantidadeEstoque", "avaliacaoMedia"].includes(
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
                          "quantidadeEstoque",
                          "avaliacaoMedia",
                        ].includes(key)
                          ? Number(e.target.value)
                          : e.target.value,
                      })
                    }
                  />
                </label>
              ))}
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
              <label>
                Objetivos (separados por vírgula)
                <input
                  value={(productForm.objetivos || []).join(", ")}
                  onChange={(e) =>
                    setProductForm({
                      ...productForm,
                      objetivos: e.target.value
                        .split(",")
                        .map((v) => v.trim())
                        .filter(Boolean),
                    })
                  }
                />
              </label>
              <label>
                Esportes (separados por vírgula)
                <input
                  value={(productForm.esportes || []).join(", ")}
                  onChange={(e) =>
                    setProductForm({
                      ...productForm,
                      esportes: e.target.value
                        .split(",")
                        .map((v) => v.trim())
                        .filter(Boolean),
                    })
                  }
                />
              </label>
              {(
                [
                  ["vegano", "Vegano"],
                  ["vegetariano", "Vegetariano"],
                  ["linhaClinica", "Linha clínica"],
                  ["lancamento", "Marcar como novidade"],
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
              {productForm.imagemUrl && (
                <img
                  className="image-preview"
                  src={productForm.imagemUrl}
                  alt="Pré-visualização"
                />
              )}
            </div>
            <footer>
              <button
                type="button"
                className="button secondary"
                onClick={() => setProductForm(null)}
              >
                Cancelar
              </button>
              <button className="button primary">Salvar produto</button>
            </footer>
          </form>
        </div>
      )}
      {couponForm && (
        <div className="modal-backdrop">
          <form className="admin-modal small" onSubmit={submitCoupon}>
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
                Desconto
                <input
                  required
                  type="number"
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
                        {u.nome}
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
              <button className="button primary">Salvar cupom</button>
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
                <span>
                  {item.quantidade}× {item.produtoNome}
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
