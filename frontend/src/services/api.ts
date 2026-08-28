import type { Address, AddressInput, AdminMetrics, Cart, CategorySummary, CollaboratorSummary, Coupon, Order, OrderDetail, Page, Product, Review, StoreInfo, User, UserRole } from '../types/api'

const API_URL = import.meta.env.VITE_API_URL || '/api/v1'
const SESSION_KEY = 'vita-fortis-session'

type Session = { user: User; password: string }
type UserPayload = User & { tipo?: UserRole; role?: UserRole }
const normalizeUser = (user: UserPayload): User => ({ ...user, tipoUsuario: user.tipoUsuario || user.tipo || user.role })

export class ApiError extends Error {
  constructor(message: string, public status: number, public details?: unknown) { super(message) }
}

export const getSession = (): Session | null => {
  try {
    const session = JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null') as (Omit<Session, 'user'> & { user: UserPayload }) | null
    return session ? { ...session, user: normalizeUser(session.user) } : null
  } catch { return null }
}
export const setSession = (session: Session | null) => session ? sessionStorage.setItem(SESSION_KEY, JSON.stringify(session)) : sessionStorage.removeItem(SESSION_KEY)

const request = async <T>(path: string, init: RequestInit = {}, authenticated = false): Promise<T> => {
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  if (init.body) headers.set('Content-Type', 'application/json')
  if (authenticated) {
    const session = getSession()
    if (!session) throw new ApiError('Faça login para continuar.', 401)
    headers.set('Authorization', `Basic ${btoa(unescape(encodeURIComponent(`${session.user.email}:${session.password}`)))}`)
  }
  let response: Response
  try { response = await fetch(`${API_URL}${path}`, { ...init, headers }) }
  catch { throw new ApiError('Não foi possível conectar à Vita Fortis. Tente novamente.', 0) }
  const contentType = response.headers.get('content-type') || ''
  const payload = response.status === 204 ? null : contentType.includes('json') ? await response.json() : await response.text()
  if (!response.ok) {
    const message = typeof payload === 'object' && payload ? (payload.message || payload.error) : payload
    throw new ApiError(message || (response.status === 401 ? 'Credenciais inválidas ou sessão expirada.' : 'Não foi possível concluir a operação.'), response.status, payload)
  }
  return payload as T
}

const query = (values: Record<string, string | number | boolean | undefined>) => {
  const params = new URLSearchParams()
  Object.entries(values).forEach(([key, value]) => value !== undefined && value !== '' && params.set(key, String(value)))
  const result = params.toString()
  return result ? `?${result}` : ''
}

export const api = {
  store: () => request<StoreInfo>('/loja'),
  products: (filters: Record<string, string | number | boolean | undefined>) => request<Page<Product>>(`/produtos${query(filters)}`),
  product: (id: string | number) => request<Product>(`/produtos/${id}`),
  reviews: (id: string | number) => request<Review[]>(`/produtos/${id}/avaliacoes`),
  createReview: (id: string | number, body: { nota: number; comentario?: string }) => request<Review>(`/produtos/${id}/avaliacoes`, { method: 'POST', body: JSON.stringify(body) }, true),
  categories: () => request<CategorySummary[]>('/produtos/categorias'),
  login: (email: string, senha: string) => request<UserPayload>('/auth/login', { method: 'POST', body: JSON.stringify({ email, senha }) }).then(normalizeUser),
  register: (body: { nome: string; email: string; senha: string; cpf: string; telefone?: string }) => request<UserPayload>('/auth/cadastro', { method: 'POST', body: JSON.stringify(body) }).then(normalizeUser),
  cart: (userId: number) => request<Cart>(`/carrinhos/${userId}`, {}, true),
  addCart: (userId: number, produtoId: number, quantidade = 1) => request<Cart>(`/carrinhos/${userId}/itens`, { method: 'POST', body: JSON.stringify({ produtoId, quantidade }) }, true),
  updateCart: (userId: number, itemId: number, quantidade: number) => request<Cart>(`/carrinhos/${userId}/itens`, { method: 'PUT', body: JSON.stringify({ itemId, quantidade }) }, true),
  removeCart: (userId: number, itemId: number, quantidade = 99999) => request<Cart>(`/carrinhos/${userId}/itens/${itemId}?quantidade=${quantidade}`, { method: 'DELETE' }, true),
  clearCart: (userId: number) => request<Cart>(`/carrinhos/${userId}/itens`, { method: 'DELETE' }, true),
  applyCoupon: (userId: number, code: string) => request<Cart>(`/carrinhos/${userId}/cupom/${encodeURIComponent(code)}`, { method: 'POST' }, true),
  removeCoupon: (userId: number) => request<Cart>(`/carrinhos/${userId}/cupom`, { method: 'DELETE' }, true),
  createOrder: (body: { usuarioId: number; itens: { produtoId: number; quantidade: number }[]; cupomId?:number; formaRecebimento: 'RETIRADA' | 'ENTREGA'; enderecoId?: number; formaPagamento: string }) => request<Order>('/pedidos', { method: 'POST', body: JSON.stringify(body) }, true),
  orders: (userId: number) => request<Order[]>(`/pedidos/usuario/${userId}`, {}, true),
  order: (id: number) => request<OrderDetail>(`/pedidos/${id}`, {}, true),
  favorites: (userId: number) => request<Product[]>(`/usuarios/${userId}/favoritos`, {}, true),
  addFavorite: (userId: number, productId: number) => request<Product>(`/usuarios/${userId}/favoritos/${productId}`, { method: 'POST' }, true),
  removeFavorite: (userId: number, productId: number) => request<void>(`/usuarios/${userId}/favoritos/${productId}`, { method: 'DELETE' }, true),
  adminProducts: (filters: Record<string, string | number | boolean | undefined>) => request<Page<Product>>(`/admin/produtos${query(filters)}`,{},true),
  saveProduct: (body: Partial<Product>, id?: number) => request<Product>(`/admin/produtos${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(body) }, true),
  setProductActive: (id: number, valor: boolean) => request<void>(`/admin/produtos/${id}/ativo?valor=${valor}`, { method: 'PATCH' }, true),
  setCommercialMetadata: (id: number, body: Pick<Product, 'objetivos'|'esportes'|'vegano'|'vegetariano'|'linhaClinica'|'lancamento'|'subcategoria'|'avaliacaoMedia'>) => request<Product>(`/admin/produtos/${id}/metadados-comerciais`, { method: 'PATCH', body: JSON.stringify(body) }, true),
  setStock: (id: number, quantidade: number) => request<Product>(`/admin/produtos/${id}/estoque?quantidade=${quantidade}`, { method: 'PATCH' }, true),
  adminOrders: () => request<Order[]>('/admin/pedidos', {}, true),
  setOrderStatus: (id: number, valor: string) => request<Order>(`/admin/pedidos/${id}/status?valor=${valor}`, { method: 'PATCH' }, true),
  approvePayment: (id:number) => request<Order>(`/admin/pedidos/${id}/pagamento/aprovar`,{method:'PATCH'},true),
  rejectPayment: (id:number) => request<Order>(`/admin/pedidos/${id}/pagamento/recusar`,{method:'PATCH'},true),
  adminUsers: () => request<User[]>('/admin/usuarios', {}, true),
  setUserActive: (id: number, valor: boolean) => request<User>(`/admin/usuarios/${id}/ativo?valor=${valor}`, { method: 'PATCH' }, true),
  setUserRole: (id: number, valor: string) => request<User>(`/admin/usuarios/${id}/tipo?valor=${valor}`, { method: 'PATCH' }, true),
  createCollaborator: (body:{nome:string;email:string;senha:string;cpf:string;telefone?:string}) => request<User>('/admin/usuarios/colaboradores',{method:'POST',body:JSON.stringify(body)},true),
  setReportPermission: (id:number,valor:boolean) => request<User>(`/admin/usuarios/${id}/permissao-relatorios?valor=${valor}`,{method:'PATCH'},true),
  coupons: () => request<Coupon[]>('/admin/cupons', {}, true),
  saveCoupon: (body: Partial<Coupon>, id?: number) => request<Coupon>(`/admin/cupons${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(body) }, true),
  setCouponActive: (id: number, valor: boolean) => request<Coupon>(`/admin/cupons/${id}/ativo?valor=${valor}`, { method: 'PATCH' }, true),
  addresses: (userId: number) => request<Address[]>(`/usuarios/${userId}/enderecos`, {}, true),
  createAddress: (userId: number, body: AddressInput) => request<Address>(`/usuarios/${userId}/enderecos`, { method: 'POST', body: JSON.stringify(body) }, true),
  metrics: (inicio: string, fim: string) => request<AdminMetrics>(`/admin/metricas${query({ inicio, fim })}`, {}, true),
  collaboratorSummary: () => request<CollaboratorSummary>('/colaborador/cashback/resumo',{},true),
}
