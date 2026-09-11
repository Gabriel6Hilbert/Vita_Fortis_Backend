import { useEffect } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { Layout } from '../components/Layout'
import { AccountPage } from '../pages/AccountPage'
import { BrandsPage, ContactPage, CuratedPage, FavoritesPage, GoalsPage, InstitutionalPage } from '../pages/ContentPages'
import { AdminPage } from '../pages/AdminPage'
import { AuthPage } from '../pages/AuthPage'
import { CartPage } from '../pages/CartPage'
import { CatalogPage } from '../pages/CatalogPage'
import { HomePage } from '../pages/HomePage'
import { OrdersPage } from '../pages/OrdersPage'
import { ProductPage } from '../pages/ProductPage'
import { CollaboratorPage } from '../pages/CollaboratorPage'

function RouteEffects() {
  const location = useLocation()
  useEffect(() => {
    const raw = location.pathname === '/' ? 'Início' : location.pathname.split('/')[1].replaceAll('-', ' ')
    document.title = `${raw.charAt(0).toUpperCase()}${raw.slice(1)} | Vita Fortis`
    if (location.hash) requestAnimationFrame(() => document.getElementById(location.hash.slice(1))?.scrollIntoView())
    else window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }, [location.pathname, location.hash])
  return null
}

export function App() { return <><RouteEffects/><Routes><Route element={<Layout />}><Route index element={<HomePage />} /><Route path="catalogo" element={<CatalogPage />} /><Route path="marcas" element={<BrandsPage />} /><Route path="objetivos" element={<GoalsPage />} /><Route path="produto/:id" element={<ProductPage />} /><Route path="entrar" element={<AuthPage />} /><Route path="sacola" element={<CartPage />} /><Route path="conta" element={<AccountPage />} /><Route path="pedidos" element={<OrdersPage />} /><Route path="admin" element={<AdminPage />} /><Route path="colaborador" element={<CollaboratorPage />} /><Route path="favoritos" element={<FavoritesPage/>}/>{['ofertas','novidades','kits'].map((path) => <Route key={path} path={path} element={<CuratedPage/>}/>) }{['sobre','politicas','faq','trabalhe-conosco'].map((path) => <Route key={path} path={path} element={<InstitutionalPage/>}/>) }<Route path="contato" element={<ContactPage/>}/><Route path="*" element={<Navigate to="/" replace />} /></Route></Routes></> }
