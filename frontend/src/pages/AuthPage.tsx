import { Eye, EyeOff, LockKeyhole, Mail, RotateCcw } from 'lucide-react'
import { useState } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../app/AuthContext'
import { api } from '../services/api'

type Mode = 'login' | 'register' | 'forgot' | 'reset'
const maskCpf=(value:string)=>value.replace(/\D/g,'').slice(0,11).replace(/(\d{3})(\d)/,'$1.$2').replace(/(\d{3})(\d)/,'$1.$2').replace(/(\d{3})(\d{1,2})$/,'$1-$2')
const validCpf=(value:string)=>{const cpf=value.replace(/\D/g,'');if(cpf.length!==11||/^(\d)\1+$/.test(cpf))return false;const digit=(size:number)=>{let sum=0;for(let i=0;i<size;i++)sum+=Number(cpf[i])*(size+1-i);const rest=(sum*10)%11;return rest===10?0:rest};return digit(9)===Number(cpf[9])&&digit(10)===Number(cpf[10])}

export function AuthPage() {
  const [params] = useSearchParams()
  const resetToken = params.get('recuperar') || ''
  const [mode, setMode] = useState<Mode>(resetToken ? 'reset' : 'login')
  const [show, setShow] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [cpfError, setCpfError] = useState('')
  const { login, register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const changeMode = (next: Mode) => {
    setMode(next)
    setError('')
    setSuccess('')
  }

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setSuccess('')
    setBusy(true)
    const data = new FormData(event.currentTarget)
    try {
      if (mode === 'forgot') {
        const result = await api.requestPasswordReset(String(data.get('email')))
        setSuccess(result.message)
        return
      }
      if (mode === 'reset') {
        const senha = String(data.get('senha'))
        const confirmacao = String(data.get('confirmacao'))
        if (senha !== confirmacao) throw new Error('As senhas informadas não coincidem.')
        const result = await api.resetPassword(resetToken, senha)
        setSuccess(result.message)
        setTimeout(() => navigate('/entrar', { replace: true }), 1600)
        return
      }
      let logged
      if (mode === 'login') logged=await login(String(data.get('email')), String(data.get('senha')))
      else { const cpf=String(data.get('cpf')); if(!validCpf(cpf)){setCpfError('Informe um CPF válido.');return} logged=await register({ nome: String(data.get('nome')), email: String(data.get('email')), senha: String(data.get('senha')), cpf, telefone: String(data.get('telefone') || '') }) }
      const state = location.state as { from?: string } | null
      navigate(logged.tipoUsuario==='ADMIN'?'/admin':logged.tipoUsuario==='COLABORADOR'?'/colaborador':state?.from||'/', { replace: true })
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Não foi possível concluir a operação.')
    } finally {
      setBusy(false)
    }
  }

  const title = mode === 'login' ? 'Boas-vindas de volta' : mode === 'register' ? 'Crie sua conta' : mode === 'forgot' ? 'Recupere seu acesso' : 'Crie uma nova senha'
  const description = mode === 'login' ? 'Entre com os dados cadastrados na Vita Fortis.' : mode === 'register' ? 'Preencha seus dados para começar.' : mode === 'forgot' ? 'Informe seu e-mail e enviaremos um link seguro.' : 'O link é válido por 30 minutos e pode ser usado uma vez.'

  return <div className="auth-page">
    <div className="auth-visual"><div><span className="eyebrow light">Vita Fortis</span><h1>Seu progresso merece companhia.</h1><p>Acesse pedidos, sacola e seus dados em um só lugar.</p></div></div>
    <section className="auth-card">
      <div className="auth-icon">{mode === 'forgot' ? <Mail /> : mode === 'reset' ? <RotateCcw /> : <LockKeyhole />}</div>
      <h2>{title}</h2><p>{description}</p>
      {(mode === 'login' || mode === 'register') && <div className="auth-switch"><button className={mode === 'login' ? 'active' : ''} onClick={() => changeMode('login')}>Entrar</button><button className={mode === 'register' ? 'active' : ''} onClick={() => changeMode('register')}>Cadastrar</button></div>}
      <form onSubmit={submit} className="form">
        {mode === 'register' && <><label>Nome completo<input name="nome" required maxLength={120} autoComplete="name" /></label><div className="form-row"><label>CPF<input name="cpf" required placeholder="000.000.000-00" inputMode="numeric" maxLength={14} onChange={e=>{e.currentTarget.value=maskCpf(e.currentTarget.value);setCpfError('')}} onBlur={e=>setCpfError(validCpf(e.currentTarget.value)?'':'Informe um CPF válido.')} aria-invalid={Boolean(cpfError)}/>{cpfError&&<small className="field-error">{cpfError}</small>}</label><label>Telefone<input name="telefone" maxLength={20} autoComplete="tel" /></label></div></>}
        {mode !== 'reset' && <label>E-mail<input name="email" required type="email" autoComplete="email" /></label>}
        {(mode === 'login' || mode === 'register' || mode === 'reset') && <label>Senha<div className="password-field"><input name="senha" required type={show ? 'text' : 'password'} minLength={mode === 'login' ? undefined : 8} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} /><button type="button" onClick={() => setShow(!show)} aria-label={show ? 'Ocultar senha' : 'Mostrar senha'}>{show ? <EyeOff /> : <Eye />}</button></div>{mode !== 'login' && <small>Mínimo de 8 caracteres.</small>}</label>}
        {mode === 'reset' && <label>Confirmar nova senha<input name="confirmacao" required type={show ? 'text' : 'password'} minLength={8} autoComplete="new-password" /></label>}
        {error && <div className="form-error" role="alert">{error}</div>}
        {success && <div className="form-success" role="status">{success}</div>}
        <button className="button primary" disabled={busy}>{busy ? 'Aguarde…' : mode === 'login' ? 'Entrar' : mode === 'register' ? 'Criar conta' : mode === 'forgot' ? 'Enviar link' : 'Redefinir senha'}</button>
      </form>
      {mode === 'login' && <button className="auth-secondary-action" onClick={() => changeMode('forgot')}>Esqueci minha senha</button>}
      {(mode === 'forgot' || mode === 'reset') && <button className="auth-secondary-action" onClick={() => { navigate('/entrar', { replace: true }); changeMode('login') }}>Voltar para entrar</button>}
      <p className="security-copy">Seus dados de acesso são protegidos e nunca enviamos sua senha por e-mail.</p>
    </section>
  </div>
}
