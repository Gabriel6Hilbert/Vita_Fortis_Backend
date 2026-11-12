import { ImageOff } from 'lucide-react'
import { useState, type ImgHTMLAttributes } from 'react'

type ProductImageProps = Omit<ImgHTMLAttributes<HTMLImageElement>, 'src' | 'onError'> & {
  src?: string | null
}

export function ProductImage({ src, alt, ...props }: ProductImageProps) {
  const [failed, setFailed] = useState(false)

  if (!src?.trim() || failed) {
    return <span className="product-image-placeholder" role="img" aria-label={alt ? alt + ' · foto indisponível' : 'Foto indisponível'}>
      <ImageOff aria-hidden="true" size={24} />
      <span aria-hidden="true">Foto indisponível</span>
    </span>
  }

  return <img {...props} src={src} alt={alt} onError={() => setFailed(true)} />
}
