import { BadgeDollarSign } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { ErrorState, Loading } from '../components/States'
import { api } from '../services/api'
import { isCollaborator, type CollaboratorSummary } from '../types/api'
import { money } from '../utils/format'

export function CollaboratorPage(){
 const {user}=useAuth(); const [data,setData]=useState<CollaboratorSummary|null>(null); const [error,setError]=useState('')
 const load=()=>{setError('');api.collaboratorSummary().then(setData).catch((e:Error)=>setError(e.message))}; useEffect(()=>{if(isCollaborator(user))void load()},[user])
 if(!user)return <Navigate to="/entrar" replace/>; if(!isCollaborator(user))return <Navigate to={user.tipoUsuario==='ADMIN'?'/admin':'/conta'} replace/>
 return <div className="page container collaborator-page"><header className="page-header compact"><span className="eyebrow">Área do colaborador</span><h1>Olá, {user.nome.split(' ')[0]}.</h1><p>Consulte o saldo acumulado do seu cashback.</p></header>{error?<ErrorState message={error} retry={load}/>:!data?<Loading/>:<section className="cashback-balance-card"><BadgeDollarSign/><span>Saldo acumulado de cashback</span><strong>{money(data.saldo)}</strong><p>Os cálculos são realizados internamente a partir dos pedidos elegíveis.</p></section>}</div>
}
