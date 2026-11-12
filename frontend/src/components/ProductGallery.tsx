import { useState } from 'react'
import type { Product } from '../types/api'

import { ProductImage } from './ProductImage'

export function ProductGallery({ product }: { product: Product }) {
  const [selected, setSelected] = useState(0)
  const images = [product.imagemUrl, ...(product.imagensAdicionais ?? [])].filter(Boolean)
  const index = Math.min(selected, Math.max(0, images.length - 1))

  return <div className="product-gallery">
    <div className="detail-image">
      <ProductImage key={index} src={images[index]} alt={images.length > 1 ? product.nome + ' · foto ' + (index + 1) : product.nome} />
    </div>
    {images.length > 1 && <div className="gallery-actions" aria-label="Galeria de fotos">
      <button className="button secondary" type="button" disabled={index === 0} onClick={() => setSelected(index - 1)}>Anterior</button>
      <span role="status">{index + 1} de {images.length}</span>
      <button className="button secondary" type="button" disabled={index === images.length - 1} onClick={() => setSelected(index + 1)}>Próxima</button>
    </div>}
  </div>
}
