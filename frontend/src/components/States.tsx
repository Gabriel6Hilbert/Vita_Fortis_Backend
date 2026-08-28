import { AlertCircle, LoaderCircle, PackageOpen, RefreshCw } from 'lucide-react'
export const Loading = ({ label = 'Carregando…' }: { label?: string }) => <div className="state"><LoaderCircle className="spin" /><p>{label}</p></div>
export const Empty = ({ title = 'Nada por aqui', text = 'Não encontramos itens para exibir.' }: { title?: string; text?: string }) => <div className="state"><PackageOpen /><h3>{title}</h3><p>{text}</p></div>
export const ErrorState = ({ message, retry }: { message: string; retry?: () => void }) => <div className="state error"><AlertCircle /><h3>Algo não saiu como esperado</h3><p>{message}</p>{retry && <button className="button secondary" onClick={retry}><RefreshCw size={16} /> Tentar novamente</button>}</div>
