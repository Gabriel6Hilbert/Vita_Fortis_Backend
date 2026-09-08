import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, getSession, setSession } from '../services/api'
import type { User } from '../types/api'

type AuthContextValue = { user: User | null; login: (email: string, password: string) => Promise<User>; register: (data: { nome: string; email: string; senha: string; cpf: string; telefone?: string }) => Promise<User>; updateUser:(user:User)=>void; logout: () => void }
const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getSession()?.user || null)
  useEffect(() => {
    if (!getSession()) return
    api.me().then((current) => {
      setSession({ user: current })
      setUser(current)
    }).catch(() => {
      setSession(null)
      setUser(null)
    })
  }, [])
  const login = async (email: string, password: string) => { const result = await api.login(email, password); setSession({ user: result }); setUser(result); return result }
  const register = async (data: { nome: string; email: string; senha: string; cpf: string; telefone?: string }) => { await api.register(data); return login(data.email,data.senha) }
  const logout = () => { void api.logout().catch(()=>undefined); setSession(null); setUser(null) }
  const updateUser=(next:User)=>{const session=getSession();if(session)setSession({...session,user:next});setUser(next)}
  return <AuthContext.Provider value={useMemo(() => ({ user, login, register, updateUser, logout }), [user])}>{children}</AuthContext.Provider>
}

export const useAuth = () => { const context = useContext(AuthContext); if (!context) throw new Error('AuthProvider ausente'); return context }
