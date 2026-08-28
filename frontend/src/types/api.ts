export type UserRole = 'ADMIN' | 'COLABORADOR' | 'CLIENTE'

export interface User { id: number; nome: string; email: string; cpf: string; telefone?: string; tipoUsuario: UserRole; ativo: boolean; permissaoRelatorios?: boolean; saldoCashback?: number }
export const isAdmin = (user: User | null | undefined) => user?.tipoUsuario === 'ADMIN'
export const isCollaborator = (user: User | null | undefined) => user?.tipoUsuario === 'COLABORADOR'
export interface StoreInfo { nome: string; descricao: string; telefone: string; email: string; instagram: string; endereco: string }
export interface Product { id: number; codigo: string; nome: string; descricao: string; marca?: string; unidade?: string; preco: number; valorDesconto?: number; precoFinal: number; descontoValor?: number; descontoPercentual?: number; quantidadeEstoque: number; categoria: string; ativo: boolean; imagemUrl?: string; objetivos?: string[]; esportes?: string[]; vegano?: boolean; vegetariano?: boolean; linhaClinica?: boolean; lancamento?: boolean; subcategoria?: string; avaliacaoMedia?: number; maisVendido?: boolean }
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; first: boolean; last: boolean }
export interface CategorySummary { categoria: string; quantidadeProdutos: number }
export interface CartItem { itemId: number; produtoId: number; produtoNome: string; quantidade: number; precoUnitario: number; subtotal: number }
export interface Cart { carrinhoId: number; usuarioId: number; carrinhoItens: CartItem[]; subtotal: number; descontos: number; total: number; cupomCodigo?: string; cupomId?: number; colaboradorNome?: string; cashbackColaborador?: number }
export interface OrderItem { id: number; produtoId: number; produtoNome: string; quantidade: number; precoUnitario: number; subtotal: number }
export interface Order { id: number; usuarioId: number; dataPedido: string; subtotal: number; desconto: number; frete: number; total: number; status: string; statusPagamento: string; formaRecebimento: 'RETIRADA' | 'ENTREGA'; prazoEntregaDias?: number; formaPagamento?:string; referenciaPagamentoMascarada?:string; itens: OrderItem[]; cupomCodigo?: string }
export interface OrderStatusEvent { status: string; data: string; descricao?: string }
export interface OrderDetail extends Order { enderecoMascarado?: string; pagamento?: { metodo?: string; status?: string }; historicoStatus?: OrderStatusEvent[] }
export interface Review { id: number; usuarioId: number; usuarioNome?: string; nota: number; comentario?: string; dataCriacao: string; status?: string }
export interface AdminMetrics { faturamento: number; pedidos: number; ticketMedio: number; produtosVendidos: number; clientes: number; comparacaoFaturamentoPercentual?: number; serieDiaria?: { periodo: string; faturamento: number; pedidos: number }[]; serieMensal?: { periodo: string; faturamento: number; pedidos: number }[]; categorias?: Record<string, number>; status?: Record<string, number>; maisVendidos?: { produtoId: number; nome: string; quantidade: number; faturamento?: number }[]; vendasRecentes?: { pedidoId:number; data:string; cliente:string; total:number; status:string }[] }
export interface Address { id: number; apelido?: string; cep: string; logradouro: string; numero: string; complemento?: string; bairro: string; cidade: string; uf: string; principal: boolean }
export type AddressInput = Omit<Address, 'id'>
export interface Coupon { id: number; codigo: string; descricao?: string; tipo: 'PERCENTUAL' | 'FIXO'; desconto: number; minSubtotal?: number; ativo: boolean; dataCadastro: string; dataVencimento?: string; colaboradorId?: number; colaboradorNome?: string; percentualCashback?: number }
export interface CashbackMovement { id:number; tipo:string; valor:number; saldoAnterior:number; saldoNovo:number; justificativa:string; pedidoId?:number; criadoEm:string }
export interface CollaboratorSummary { colaboradorId:number; saldo:number; cashbackConfirmado:number; cashbackEstornado:number; vendasGeradas:number; pedidosGerados:number; cupons:{id:number;codigo:string;ativo:boolean;percentualCashback:number;pedidos:number;vendas:number;cashback:number}[]; movimentos:CashbackMovement[]; pedidos:Order[] }

