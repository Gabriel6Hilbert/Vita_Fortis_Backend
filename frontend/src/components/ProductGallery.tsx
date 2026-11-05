import { useState } from 'react'
import type { Product } from '../types/api'

const fallback = '/assets/imagens/proteina_exemplo.png'

export function ProductGallery({ product }: { product: Product }) {
  const [selected, setSelected] = useState(0)
  const images = [product.imagemUrl, ...(product.imagensAdicionais ?? [])].filter(Boolean)
  const index = Math.min(selected, Math.max(0, images.length - 1))

  return <div className="product-gallery">
    <div className="detail-image">
      <img src={images[index] || fallback} alt={images.length > 1 ? product.nome + ' · foto ' + (index + 1) : product.nome}
        onError={event => { if (!event.currentTarget.src.endsWith(fallback)) event.currentTarget.src = fallback }} />
    </div>
    {images.length > 1 && <div className="gallery-actions" aria-label="Galeria de fotos">
      <button className="button secondary" type="button" disabled={index === 0} onClick={() => setSelected(index - 1)}>Anterior</button>
      <span role="status">{index + 1} de {images.length}</span>
      <button className="button secondary" type="button" disabled={index === images.length - 1} onClick={() => setSelected(index + 1)}>Próxima</button>
    </div>}
  </div>
}
