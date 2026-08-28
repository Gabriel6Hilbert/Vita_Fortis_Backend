import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { api, getSession, setSession } from '../services/api'
import type { User } from '../types/api'

type AuthContextValue = { user: User | null; login: (email: string, password: string) => Promise<User>; register: (data: { nome: string; email: string; senha: string; cpf: string; telefone?: string }) => Promise<User>; logout: () => void }
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getSession()?.user || null)
  const login = async (email: string, password: string) => { const result = await api.login(email, password); setSession({ user: result, password }); setUser(result); return result }
  const register = async (data: { nome: string; email: string; senha: string; cpf: string; telefone?: string }) => { const result = await api.register(data); setSession({ user: result, password: data.senha }); setUser(result); return result }
  const logout = () => { setSession(null); setUser(null) }
  return <AuthContext.Provider value={useMemo(() => ({ user, login, register, logout }), [user])}>{children}</AuthContext.Provider>
}

export const useAuth = () => { const context = useContext(AuthContext); if (!context) throw new Error('AuthProvider ausente'); return context }
