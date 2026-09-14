import { ArrowRight, Minus, Plus, Trash2 } from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../app/AuthContext";
import { useCart } from "../app/CartContext";
import { Empty, ErrorState, Loading } from "../components/States";
import { api, type OrderInput } from "../services/api";
import type { Address, AddressInput, Order } from "../types/api";
import { money } from "../utils/format";

const emptyAddress: AddressInput = {
  apelido: "Casa",
  cep: "",
  logradouro: "",
  numero: "",
  complemento: "",
  bairro: "",
  cidade: "",
  uf: "",
  principal: true,
};
const UFS=['AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'];

export function CartPage() {
  const { user } = useAuth();
  const {
    cart,
    loading,
    error: cartError,
    total,
    update,
    remove,
    clear,
    refresh,
  } = useCart();
  const [coupon, setCoupon] = useState("");
  const [message, setMessage] = useState("");
  const [ordering, setOrdering] = useState(false);
  const [clearing, setClearing] = useState(false);
  const [reviewing, setReviewing] = useState(false);
  const [quote, setQuote] = useState<Order | null>(null);
  const [reviewedInput, setReviewedInput] = useState<OrderInput | null>(null);
  const [receiving, setReceiving] = useState<"RETIRADA" | "ENTREGA">(
    "RETIRADA",
  );
  const [payment, setPayment] = useState("PIX");
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [addressId, setAddressId] = useState<number | undefined>();
  const [newAddress, setNewAddress] = useState<AddressInput>(emptyAddress);
  const [cepStatus,setCepStatus]=useState('');
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) return;
    api
      .addresses(user.id)
      .then((items) => {
        setAddresses(items);
        setAddressId(items.find((item) => item.principal)?.id ?? items[0]?.id);
      })
      .catch(() => undefined);
  }, [user]);

  useEffect(() => {
    setReviewing(false);
    setQuote(null);
    setReviewedInput(null);
  }, [cart, user?.id]);

  if (loading && !cart)
    return (
      <div className="page container">
        <Loading />
      </div>
    );
  if (cartError && !cart)
    return (
      <div className="page container">
        <ErrorState message={cartError} retry={refresh} />
      </div>
    );
  if (!cart?.carrinhoItens.length)
    return (
      <div className="page container">
        <Empty
          title="Sua sacola está vazia"
          text="Explore o catálogo para encontrar produtos para seu objetivo."
        />
        <div className="center">
          <Link className="button primary" to="/catalogo">
            Explorar produtos
          </Link>
        </div>
      </div>
    );

  const finish = async (event: FormEvent) => {
    event.preventDefault();
    if (!user) { navigate('/entrar', { state: { from: '/sacola' } }); return; }
    if (ordering) return;
    setOrdering(true);
    setMessage("");
    try {
      let selectedAddressId = addressId;
      if (receiving === "ENTREGA" && !selectedAddressId) {
        const address = await api.createAddress(user.id, newAddress);
        selectedAddressId = address.id;
        setAddresses((current) => [...current, address]);
        setAddressId(address.id);
      }
      const input: OrderInput = {
        usuarioId: user.id,
        itens: cart.carrinhoItens.map((item) => ({
          produtoId: item.produtoId,
          quantidade: item.quantidade,
        })),
        cupomId: cart.cupomId,
        formaRecebimento: receiving,
        enderecoId: receiving === "ENTREGA" ? selectedAddressId : undefined,
        formaPagamento: payment,
      };
      if (!reviewing || !quote || !reviewedInput) {
        const result = await api.reviewOrder(input);
        setReviewedInput(input);
        setQuote(result);
        if (selectedAddressId) setAddressId(selectedAddressId);
        setReviewing(true);
        setMessage("Confira o frete, o prazo e o total calculados antes de confirmar.");
        return;
      }
      const order = await api.createOrder({ ...reviewedInput, totalRevisado: Number(quote.total) });
      // A compra já foi criada. Uma falha ao limpar a sacola não deve permitir repeti-la.
      try { await clear(); } catch { /* O pedido permanece acessível em Meus pedidos. */ }
      navigate(`/pedidos?novo=${order.id}`);
    } catch (error) {
      setReviewing(false);
      setQuote(null);
      setReviewedInput(null);
      setMessage(
        error instanceof Error
          ? error.message
          : "Não foi possível criar o pedido.",
      );
    } finally {
      setOrdering(false);
    }
  };

  const applyCoupon = async () => {
    if (!user) { setMessage('Entre na sua conta para aplicar um cupom.'); return; }
    try {
      await api.applyCoupon(user.id, coupon.trim());
      await refresh();
      setMessage("Cupom aplicado.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Cupom inválido.");
    }
  };

  const clearCart = async () => {
    if (!window.confirm("Deseja remover todos os produtos da sacola?")) return;
    setClearing(true);
    setMessage("");
    try {
      await clear();
    } catch (error) {
      setMessage(
        error instanceof Error
          ? error.message
          : "Não foi possível limpar a sacola.",
      );
    } finally {
      setClearing(false);
    }
  };

  const field = (key: keyof AddressInput, value: string) =>
    setNewAddress((current) => ({ ...current, [key]: value }));
  const changeCart = async (action: () => Promise<void>) => {
    setReviewing(false); setQuote(null); setReviewedInput(null);
    try { await action(); } catch (error) { setMessage(error instanceof Error ? error.message : 'Não foi possível atualizar a sacola.'); }
  };
  const lookupCep=async()=>{const cep=newAddress.cep.replace(/\D/g,'');if(cep.length!==8){setCepStatus('Informe os 8 dígitos do CEP.');return}setCepStatus('Consultando CEP…');try{const response=await fetch(`https://viacep.com.br/ws/${cep}/json/`);if(!response.ok)throw new Error();const data=await response.json();if(data.erro)throw new Error();setNewAddress(current=>({...current,cep,logradouro:data.logradouro||current.logradouro,bairro:data.bairro||current.bairro,cidade:data.localidade||current.cidade,uf:data.uf||current.uf}));setCepStatus('Endereço localizado. Confira o número e os dados.') }catch{setCepStatus('CEP não encontrado. Confira e tente novamente.')}};

  return (
    <div className="page container">
      <header className="page-header compact">
        <span className="eyebrow">Sua seleção</span>
        <h1>Sacola e checkout</h1>
      </header>
      <form className="cart-layout" onSubmit={finish}>
        <section className="cart-items">
          {cart.carrinhoItens.map((original) => { const priced = quote?.itens.find(item => item.produtoId === original.produtoId); const item = priced ? { ...original, precoUnitario: priced.precoUnitario, subtotal: priced.subtotal } : original; return (
            <article className="cart-item" key={item.itemId}>
              <div className="cart-thumb">
                <img
                  src={
                    item.produtoImagemUrl ||
                    "/assets/imagens/proteina_exemplo.png"
                  }
                  onError={(event) => {
                    event.currentTarget.src =
                      "/assets/imagens/proteina_exemplo.png";
                  }}
                  alt={item.produtoNome}
                />
              </div>
              <div className="cart-item-main">
                <h2>{item.produtoNome}</h2>
                <p>{money(item.precoUnitario)} por unidade</p>
                <div className="quantity small">
                  <button
                    type="button"
                    onClick={() =>
                      void changeCart(() => update(item.itemId, Math.max(1, item.quantidade - 1)))
                    }
                    aria-label="Diminuir"
                  >
                    <Minus />
                  </button>
                  <span>{item.quantidade}</span>
                  <button
                    type="button"
                    onClick={() => void changeCart(() => update(item.itemId, item.quantidade + 1))}
                    aria-label="Aumentar"
                  >
                    <Plus />
                  </button>
                </div>
              </div>
              <div className="cart-item-price">
                <strong>{money(item.subtotal)}</strong>
                <button
                  type="button"
                  onClick={() => { if(window.confirm(`Remover ${item.produtoNome} da sacola?`)) void changeCart(() => remove(item.itemId)) }}
                  aria-label="Remover"
                >
                  <Trash2 />
                </button>
              </div>
            </article>
          )})}
          <button
            type="button"
            className="text-button danger clear-cart"
            onClick={clearCart}
            disabled={clearing}
          >
            <Trash2 size={16} /> {clearing ? "Limpando…" : "Limpar sacola"}
          </button>
        </section>
        <aside className="summary">
          <h2>Finalização</h2>
          {reviewing && (
            <div className="review-alert">
              <strong>Revisão final</strong>
              <span>
                Confira itens, entrega, pagamento, desconto e total. O pagamento
                ficará pendente para confirmação administrativa.
              </span>
            </div>
          )}
          <div className="checkout-section">
            <div className="review-alert"><strong>Pagamento simulado — ambiente de homologação</strong><span>Nenhum dado de cartão, CVV, boleto ou Pix real é coletado. A confirmação é administrativa.</span></div>
            <label className="checkout-field">
              Como deseja receber?
              <select
                disabled={reviewing}
                value={receiving}
                onChange={(event) =>
                  setReceiving(event.target.value as "RETIRADA" | "ENTREGA")
                }
              >
                <option value="RETIRADA">Retirar na loja</option>
                <option value="ENTREGA">Receber no endereço</option>
              </select>
            </label>
          </div>
          {receiving === "ENTREGA" &&
            (addresses.length ? (
              <div className="checkout-section address-section-cart">
                <label className="checkout-field">
                  Endereço
                  <select
                    value={addressId}
                    disabled={reviewing || ordering}
                    onChange={(event) =>
                      setAddressId(Number(event.target.value))
                    }
                  >
                    {addresses.map((address) => (
                      <option key={address.id} value={address.id}>
                        {address.apelido || address.logradouro} - {address.cep}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
            ) : (
              <div className="checkout-section address-section-cart">
                <strong className="section-label">Endereço de entrega</strong>
                <div className="form-grid cart-address-grid">
                  <label>
                    CEP
                    <input
                      required
                      pattern="[0-9]{8}"
                      value={newAddress.cep}
                      onChange={(event) =>
                        field("cep", event.target.value.replace(/\D/g, ""))
                      }
                      onBlur={()=>void lookupCep()}
                    />
                    {cepStatus&&<small>{cepStatus}</small>}
                  </label>
                  <label>
                    Logradouro
                    <input
                      required
                      value={newAddress.logradouro}
                      onChange={(event) =>
                        field("logradouro", event.target.value)
                      }
                    />
                  </label>
                  <label>
                    Número
                    <input
                      required
                      value={newAddress.numero}
                      onChange={(event) => field("numero", event.target.value)}
                    />
                  </label>
                  <label>
                    Complemento
                    <input
                      value={newAddress.complemento}
                      onChange={(event) =>
                        field("complemento", event.target.value)
                      }
                    />
                  </label>
                  <label>
                    Bairro
                    <input
                      required
                      value={newAddress.bairro}
                      onChange={(event) => field("bairro", event.target.value)}
                    />
                  </label>
                  <label>
                    Cidade
                    <input
                      required
                      value={newAddress.cidade}
                      onChange={(event) => field("cidade", event.target.value)}
                    />
                  </label>
                  <label>
                    UF
                    <select required value={newAddress.uf} onChange={(event)=>field("uf",event.target.value)}><option value="">Selecione</option>{UFS.map(uf=><option key={uf}>{uf}</option>)}</select>
                  </label>
                </div>
              </div>
            ))}
          <div className="checkout-section">
            <label className="checkout-field">
              Pagamento
              <select
                disabled={reviewing}
                value={payment}
                onChange={(event) => setPayment(event.target.value)}
              >
                <option value="PIX">Pix simulado</option>
                <option value="CARTAO">Cartão simulado</option>
                <option value="BOLETO">Boleto simulado</option>
              </select>
            </label>
          </div>
          <div className="summary-line">
            <span>Subtotal</span>
            <strong>{money(quote?.subtotal ?? cart.subtotal ?? total)}</strong>
          </div>
          {(cart.descontos || 0) > 0 && (
            <div className="summary-line discount-line">
              <span>Desconto ({cart.cupomCodigo})</span>
              <strong>-{money(quote?.desconto ?? cart.descontos)}</strong>
            </div>
          )}
          <div className="summary-line">
            <span>Frete</span>
            <span>
              {receiving === "RETIRADA" ? "Grátis" : quote ? money(quote.frete) : "Disponível na revisão"}
            </span>
          </div>
          {quote && receiving === "ENTREGA" && <p className="summary-message">Prazo estimado: {quote.prazoEntregaDias} dias. Frete de homologação com tarifa fixa.</p>}
          <div className="coupon">
            <label>
              Cupom
              <input
                value={coupon}
                disabled={reviewing || ordering}
                onChange={(event) => setCoupon(event.target.value)}
                placeholder="Digite o código"
              />
            </label>
            <button type="button" disabled={reviewing || ordering} onClick={applyCoupon}>
              Aplicar
            </button>
          </div>
          {(cart.cashbackColaborador || 0) > 0 && (
            <p className="cashback-disclosure">
              Este pedido gerará {money(cart.cashbackColaborador)} de cashback
              para {cart.colaboradorNome}. Esse valor não é desconto nem crédito
              para o cliente.
            </p>
          )}
          <div className="summary-total">
            <span>{receiving === 'ENTREGA' && !quote ? 'Total sem frete' : 'Total'}</span>
            <strong>{money(quote?.total ?? cart.total ?? total)}</strong>
          </div>
          {(message || cartError) && <p className="summary-message" role="status">{message || cartError}</p>}
          {reviewing && (
            <button
              type="button"
              className="button secondary"
              onClick={() => {
                setReviewing(false);
                setQuote(null);
                setReviewedInput(null);
                setMessage("");
              }}
            >
              Voltar e editar
            </button>
          )}
          <button className="button primary" disabled={ordering}>
            {ordering ? (
              "Finalizando…"
            ) : reviewing ? (
              <>
                Confirmar pedido <ArrowRight size={18} />
              </>
            ) : (
              <>
                {user ? 'Revisar pedido' : 'Entrar para finalizar'} <ArrowRight size={18} />
              </>
            )}
          </button>
        </aside>
      </form>
    </div>
  );
}
