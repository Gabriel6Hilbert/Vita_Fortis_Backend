import type { CatalogRegistration, Address, AddressInput, AdminMetrics, BrandSummary, Cart, CategorySummary, CollaboratorSummary, Coupon, GoalSummary, Order, OrderDetail, Page, Product, Review, StockMovement, StoreInfo, User, UserRole } from '../types/api'

const API_URL = import.meta.env.VITE_API_URL || '/api/v1'
const SESSION_KEY = 'vita-fortis-session'
const PUBLIC_CACHE_MS = 30_000
const publicCache = new Map<string, { expiresAt: number; promise: Promise<unknown> }>()

type Session = { user: User }
export type OrderFilters = Partial<Record<'numero' | 'inicio' | 'fim' | 'cliente' | 'status' | 'recebimento' | 'pagamento' | 'statusPagamento', string>>
type UserPayload = User & { tipo?: UserRole; role?: UserRole }
export type OrderInput = { usuarioId: number; itens: { produtoId: number; quantidade: number }[]; cupomId?:number; formaRecebimento: 'RETIRADA' | 'ENTREGA'; enderecoId?: number; formaPagamento: string; totalRevisado?: number }
const normalizeUser = (user: UserPayload): User => ({ ...user, tipoUsuario: user.tipoUsuario || user.tipo || user.role })

export class ApiError extends Error {
  constructor(message: string, public status: number, public details?: unknown) { super(message) }
}

const apiErrorMessage = (payload: unknown, status: number) => {
  if (!payload || typeof payload !== 'object') {
    return typeof payload === 'string' && payload.trim()
      ? payload
      : status === 401 ? 'Credenciais inválidas ou sessão expirada.' : 'Não foi possível concluir a operação.'
  }
  const data = payload as Record<string, unknown>
  const details = data.detalhes
  if (details && typeof details === 'object') {
    const fieldMessages = Object.values(details as Record<string, unknown>)
      .filter((value): value is string => typeof value === 'string' && Boolean(value.trim()))
    if (fieldMessages.length) return fieldMessages.join(' • ')
  }
  const message = data.mensagem || data.message || data.erro || data.error
  return typeof message === 'string' && message.trim()
    ? message
    : status === 401 ? 'Credenciais inválidas ou sessão expirada.' : 'Não foi possível concluir a operação.'
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
  if (init.body && !(init.body instanceof FormData)) headers.set('Content-Type', 'application/json')
  if (authenticated) {
    if (!getSession()) throw new ApiError('Faça login para continuar.', 401)
  }
  let response: Response
  try { response = await fetch(`${API_URL}${path}`, { ...init, headers, credentials:'include' }) }
  catch { throw new ApiError('Não foi possível conectar à Vita Fortis. Tente novamente.', 0) }
  const contentType = response.headers.get('content-type') || ''
  const payload = response.status === 204 ? null : contentType.includes('json') ? await response.json() : await response.text()
  if (!response.ok) {
    throw new ApiError(apiErrorMessage(payload, response.status), response.status, payload)
  }
  if (init.method && init.method !== 'GET') publicCache.clear()
  return payload as T
}

const publicRequest = <T>(path: string): Promise<T> => {
  const cached = publicCache.get(path)
  if (cached && cached.expiresAt > Date.now()) return cached.promise as Promise<T>
  const promise = request<T>(path).catch(error => { publicCache.delete(path); throw error })
  publicCache.set(path, { expiresAt: Date.now() + PUBLIC_CACHE_MS, promise })
  return promise
}

const query = (values: Record<string, string | number | boolean | undefined>) => {
  const params = new URLSearchParams()
  Object.entries(values).forEach(([key, value]) => value !== undefined && value !== '' && params.set(key, String(value)))
  const result = params.toString()
  return result ? `?${result}` : ''
}

export const api = {
  catalogRegistrations: () => request<CatalogRegistration[]>('/admin/catalogo/cadastros', {}, true),
  saveCatalogRegistration: (body: Partial<CatalogRegistration>) => request<CatalogRegistration>('/admin/catalogo/cadastros' + (body.id ? '/' + body.id : ''), { method: body.id ? 'PUT' : 'POST', body: JSON.stringify(body) }, true),
  disableCatalogRegistration: (id: number) => request<void>('/admin/catalogo/cadastros/' + id, { method: 'DELETE' }, true),
  store: () => request<StoreInfo>('/loja'),
  products: (filters: Record<string, string | number | boolean | undefined>) => publicRequest<Page<Product>>(`/produtos${query(filters)}`),
  product: (id: string | number) => publicRequest<Product>(`/produtos/${id}`),
  reviews: (id: string | number) => request<Review[]>(`/produtos/${id}/avaliacoes`),
  createReview: (id: string | number, body: { usuarioId:number; nota: number; comentario?: string }) => request<Review>(`/produtos/${id}/avaliacoes`, { method: 'POST', body: JSON.stringify(body) }, true),
  categories: () => publicRequest<CategorySummary[]>('/produtos/categorias'),
  brands: () => publicRequest<BrandSummary[]>('/produtos/marcas'),
  goals: () => publicRequest<GoalSummary[]>('/produtos/objetivos'),
  login: (email: string, senha: string) => request<UserPayload>('/auth/login', { method: 'POST', body: JSON.stringify({ email, senha }) }).then(normalizeUser),
  register: (body: { nome: string; email: string; senha: string; cpf: string; telefone?: string }) => request<UserPayload>('/auth/cadastro', { method: 'POST', body: JSON.stringify(body) }).then(normalizeUser),
  requestPasswordReset: (email: string) => request<{message:string}>('/auth/recuperacao-senha', { method: 'POST', body: JSON.stringify({ email }) }),
  resetPassword: (token: string, novaSenha: string) => request<{message:string}>('/auth/redefinicao-senha', { method: 'POST', body: JSON.stringify({ token, novaSenha }) }),
  logout: () => request<void>('/auth/logout',{method:'POST'},true),
  cart: (userId: number) => request<Cart>(`/carrinhos/${userId}`, {}, true),
  addCart: (userId: number, produtoId: number, quantidade = 1) => request<Cart>(`/carrinhos/${userId}/itens`, { method: 'POST', body: JSON.stringify({ produtoId, quantidade }) }, true),
  updateCart: (userId: number, itemId: number, quantidade: number) => request<Cart>(`/carrinhos/${userId}/itens`, { method: 'PUT', body: JSON.stringify({ itemId, quantidade }) }, true),
  removeCart: (userId: number, itemId: number, quantidade = 99999) => request<Cart>(`/carrinhos/${userId}/itens/${itemId}?quantidade=${quantidade}`, { method: 'DELETE' }, true),
  clearCart: (userId: number) => request<Cart>(`/carrinhos/${userId}/itens`, { method: 'DELETE' }, true),
  applyCoupon: (userId: number, code: string) => request<Cart>(`/carrinhos/${userId}/cupom/${encodeURIComponent(code)}`, { method: 'POST' }, true),
  removeCoupon: (userId: number) => request<Cart>(`/carrinhos/${userId}/cupom`, { method: 'DELETE' }, true),
  reviewOrder: (body: OrderInput) => request<Order>('/pedidos/revisao', { method: 'POST', body: JSON.stringify(body) }, true),
  createOrder: (body: OrderInput) => request<Order>('/pedidos', { method: 'POST', body: JSON.stringify(body) }, true),
  orders: (userId: number) => request<Order[]>(`/pedidos/usuario/${userId}`, {}, true),
  order: (id: number) => request<OrderDetail>(`/pedidos/${id}`, {}, true),
  me: () => request<User>('/me', {}, true),
  updateMe: (body:{nome:string;telefone?:string;aceitaComunicacoes:boolean}) => request<User>('/me',{method:'PUT',body:JSON.stringify(body)},true),
  changePassword: (body:{senhaAtual:string;novaSenha:string}) => request<void>('/me/senha',{method:'PUT',body:JSON.stringify(body)},true),
  favorites: (userId: number) => request<Product[]>(`/usuarios/${userId}/favoritos`, {}, true),
  addFavorite: (userId: number, productId: number) => request<Product>(`/usuarios/${userId}/favoritos/${productId}`, { method: 'POST' }, true),
  removeFavorite: (userId: number, productId: number) => request<void>(`/usuarios/${userId}/favoritos/${productId}`, { method: 'DELETE' }, true),
  adminProducts: (filters: Record<string, string | number | boolean | undefined>) => request<Page<Product>>(`/admin/produtos${query(filters)}`,{},true),
  saveProduct: (body: Partial<Product>, id?: number) => request<Product>(`/admin/produtos${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', body: JSON.stringify(body) }, true),
  uploadProductImage: (file: File) => {
    const body = new FormData()
    body.append('imagem', file)
    return request<{ url: string }>('/admin/produtos/imagens', { method: 'POST', body }, true)
  },
  setProductActive: (id: number, valor: boolean) => request<void>(`/admin/produtos/${id}/ativo?valor=${valor}`, { method: 'PATCH' }, true),
  archiveProduct: (id:number) => request<{resultado:string}>(`/admin/produtos/${id}`,{method:'DELETE'},true),
  importProducts: async(file:File,preVisualizar=true) => { const body=new FormData();body.append('arquivo',file);const response=await fetch(`${API_URL}/admin/produtos/importacao?preVisualizar=${preVisualizar}`,{method:'POST',body,credentials:'include'});const payload=await response.json();if(!response.ok)throw new ApiError(apiErrorMessage(payload,response.status),response.status,payload);return payload as {preVisualizacao:boolean;inseridos:number;atualizados:number;ignorados:number;rejeitados:number;erros:string[];registros:{codigo:string;nome:string;preco:number;estoque:number;categoria:string;acao:string}[]} },
  importHistory: () => request<import('../types/api').ImportHistory[]>('/admin/produtos/importacao/historico',{},true),
  importHistoryFileUrl: (id:number) => `${API_URL}/admin/produtos/importacao/historico/${id}/arquivo`,
  setCommercialMetadata: (id: number, body: Pick<Product, 'objetivos'|'esportes'|'vegano'|'vegetariano'|'linhaClinica'|'lancamento'|'destaque'|'oferta'|'kit'|'subcategoria'|'avaliacaoMedia'>) => request<Product>(`/admin/produtos/${id}/metadados-comerciais`, { method: 'PATCH', body: JSON.stringify(body) }, true),
  setStock: (id: number, quantidade: number, motivo:string) => request<Product>(`/admin/produtos/${id}/estoque${query({quantidade,motivo})}`, { method: 'PUT' }, true),
  stockHistory: (id:number) => request<StockMovement[]>(`/admin/produtos/${id}/estoque/movimentacoes`,{},true),
  setDiscountPercent: (id:number,valor:number) => request<Product>(`/admin/produtos/${id}/desconto-percentual?valor=${valor}`,{method:'PATCH'},true),
  setDiscountValue: (id:number,valor:number) => request<Product>(`/admin/produtos/${id}/desconto-valor?valor=${valor}`,{method:'PATCH'},true),
  clearDiscount: (id:number) => request<Product>(`/admin/produtos/${id}/desconto`,{method:'DELETE'},true),
  adminOrders: (filters: OrderFilters = {}) => request<Order[]>(`/admin/pedidos${query(filters)}`, {}, true),
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
  updateAddress: (userId:number,id:number,body:AddressInput) => request<Address>(`/usuarios/${userId}/enderecos/${id}`,{method:'PUT',body:JSON.stringify(body)},true),
  removeAddress: (userId:number,id:number) => request<void>(`/usuarios/${userId}/enderecos/${id}`,{method:'DELETE'},true),
  metrics: (inicio: string, fim: string) => request<AdminMetrics>(`/admin/metricas${query({ inicio, fim })}`, {}, true),
  collaboratorSummary: () => request<CollaboratorSummary>('/colaborador/cashback/resumo',{},true),
  adjustCashback: (id:number,valor:number,justificativa:string) => request(`/admin/colaboradores/${id}/cashback/ajustes`,{method:'POST',body:JSON.stringify({valor,justificativa})},true),
  withdrawCashback: (id:number,valor:number,justificativa:string) => request(`/admin/colaboradores/${id}/cashback/baixas`,{method:'POST',body:JSON.stringify({valor,justificativa})},true),
  adminReviews: () => request<Review[]>('/admin/avaliacoes',{},true),
  moderateReview: (id:number,valor:boolean) => request<Review>(`/admin/avaliacoes/${id}/aprovada?valor=${valor}`,{method:'PATCH'},true),
  downloadReport: async (tipo:string,inicio:string,fim:string,formato:'csv'|'pdf'|'xlsx'='csv',filtros:Record<string,string>={}) => {
    const response=await fetch(`${API_URL}/admin/relatorios/${tipo}.${formato}${query({...filtros,inicio,fim})}`,{credentials:'include'})
    if(!response.ok) { const payload=await response.json().catch(()=>null); throw new ApiError(apiErrorMessage(payload,response.status),response.status) }
    const blob=await response.blob(); const url=URL.createObjectURL(blob); const a=document.createElement('a'); a.href=url; a.download=`vita-fortis-${tipo.toLowerCase()}.${formato}`; a.click(); URL.revokeObjectURL(url)
  },
}
