"""Extrai produtos e imagens dos catálogos Integralmédica e Darkness.

O resultado é um catálogo idempotente para o inicializador Spring. Os produtos
são rascunhos sem preço/estoque; a ativação continua sendo uma decisão comercial.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import uuid
from pathlib import Path

import pdfplumber
from PIL import Image, ImageOps
from pypdf import PdfReader


IM_PRODUCTS = [
    (4, 3, "MY WHEY PROTEIN", "PROTEINAS", "POTE 900 G"),
    (5, 4, "ISO TRIPLE ZERO", "PROTEINAS", "POTE 900 G"),
    (6, 3, "WHEY 100% PURE", "PROTEINAS", "POTE/POUCH"),
    (7, 5, "WHEY 100% PURE SACHÊS", "PROTEINAS", "DISPLAY 10 SACHÊS"),
    (8, 3, "BEEF PROTEIN ZEROLAC", "PROTEINAS", "POTE 900 G"),
    (9, 2, "VO2 WHEY BAR", "PROTEINAS", "CX 12 X 30 G"),
    (10, 3, "PROTEIN CRISP BAR", "PROTEINAS", "CX 12 X 45 G"),
    (11, 3, "PROTEIN CRISP OVOMALTINE", "PROTEINAS", "CX 12 X 45 G"),
    (13, 3, "ENERGY GEL VO2 C30", "CARBOIDRATOS", "DISPLAY 10 X 45 G"),
    (14, 5, "ENERGY GEL VO2 C30 COM CAFEÍNA", "CARBOIDRATOS", "DISPLAY 10 X 45 G"),
    (16, 3, "BETA ALANINA PURE", "AMINOACIDOS", "POTE 123 G"),
    (17, 3, "BCAA TOP", "AMINOACIDOS", "POTE 120 CÁPS"),
    (18, 2, "GLUTAMINE ISOLATES", "AMINOACIDOS", "POTE 150/300 G"),
    (19, 3, "BCAA 2044 MG", "AMINOACIDOS", "POTE 90 CÁPS"),
    (21, 9, "CREATINA HARDCORE", "AMINOACIDOS", "POTE/POUCH"),
    (22, 3, "CREATINA HARDCORE CREAPURE", "AMINOACIDOS", "POTE 300 G"),
    (23, 3, "CREATINA HARDCORE SABORES", "AMINOACIDOS", "POTE 350 G"),
    (24, 4, "CREATINA SABORES STICK", "AMINOACIDOS", "DISPLAY 20 STICKS"),
    (25, 4, "CREATINA HARDCORE CÁPSULAS", "AMINOACIDOS", "POTE 60/120 CÁPS"),
    (27, 4, "NUTRI WHEY GAINER", "CARBOIDRATOS", "POUCH 1,2/2,4 KG"),
    (28, 4, "CREAMASS HYPERCALORIC", "CARBOIDRATOS", "POUCH 3 KG"),
    (30, 3, "PRIME MD PRE-WORKOUT", "PRETREINOS", "POTE 300 G"),
    (31, 3, "HUGER PRE-WORKOUT", "PRETREINOS", "POTE 320 G"),
    (33, 3, "THERMA PRO HARDCORE", "TERMOGENICOS", "POTE 60/120 CÁPS"),
    (35, 2, "ZMA TESTO BOOSTER", "VITAMINAS", "POTE 60 CÁPS"),
]

DARKNESS_PRODUCTS = [
    (2, "DARK WHEY", "PROTEINAS", "POTE 900 G", "Proteína concentrada do soro do leite para aporte proteico, recuperação e ganho de massa muscular."),
    (3, "CARNIBOL", "PROTEINAS", "POTE 900 G", "Proteína da carne para aporte proteico, recuperação muscular e ganho de massa muscular."),
    (4, "CREA PURE", "AMINOACIDOS", "POTE 300 G", "Creatina monohidratada Creapure para força, desempenho e ganho de massa muscular."),
    (5, "CREATINE", "AMINOACIDOS", "POTE 300 G", "Creatina monohidratada para força, desempenho físico e ganho de massa muscular."),
    (6, "ÉVORA PW", "PRETREINOS", "POTE 300 G", "Pré-treino para energia, foco, resistência muscular e alta performance."),
    (7, "ÉVORA GX", "PRETREINOS", "POTE 300 G", "Pré-treino para energia extrema, resistência, redução da fadiga muscular e performance."),
    (8, "ÉVORA DRINK", "PRETREINOS", "LATA 269 ML", "Bebida pré-treino pronta para consumo, voltada a energia, foco e resistência."),
    (9, "ÉVORA STICK", "PRETREINOS", "DISPLAY 20 STICKS", "Pré-treino em sachê de dose única, prático para energia, foco e performance."),
    (10, "ÉVORA VULCAN", "PRETREINOS", "POTE 300 G", "Pré-treino termogênico para energia, resistência, redução da fadiga e performance."),
    (11, "VASCULOR PÓ", "PRETREINOS", "POTE 300 G", "Pré-treino vasodilatador para pump muscular, resistência e aumento de força."),
    (12, "OXYDROL", "VITAMINAS", "POTE 120 CÁPS", "Suplemento em cápsulas voltado ao suporte de força e desempenho nos treinos."),
    (13, "DARK BAR", "PROTEINAS", "DISPLAY BARRAS", "Barra proteica para reposição de proteínas, recuperação e manutenção da massa muscular."),
]


def objectives(category: str) -> list[str]:
    return {
        "PROTEINAS": ["GANHO_DE_MASSA", "PERFORMANCE"],
        "CARBOIDRATOS": ["GANHO_DE_MASSA", "PERFORMANCE"],
        "AMINOACIDOS": ["GANHO_DE_MASSA", "PERFORMANCE"],
        "PRETREINOS": ["PERFORMANCE"],
        "TERMOGENICOS": ["EMAGRECIMENTO", "PERFORMANCE"],
        "VITAMINAS": ["SAUDE_E_BEM_ESTAR"],
    }[category]


def image_name(code: str) -> str:
    return f"{uuid.uuid5(uuid.NAMESPACE_URL, 'vita-fortis/catalogos/' + code)}.webp"


def save_product_image(image: Image.Image, target: Path, background: str) -> None:
    image = image.convert("RGBA")
    image.thumbnail((1000, 1000), Image.Resampling.LANCZOS)
    square = Image.new("RGBA", (1000, 1000), background)
    square.alpha_composite(image, ((1000 - image.width) // 2, (1000 - image.height) // 2))
    square.convert("RGB").save(target, "WEBP", quality=88, method=6)


def compact_text(text: str, name: str) -> str:
    compact = re.sub(r"\s+", " ", text or "").strip()
    if compact.upper().startswith(name.upper()):
        compact = compact[len(name):].lstrip(" .:-")
    return compact[:980].rstrip()


def build(im_pdf: Path, darkness_pdf: Path, output: Path) -> None:
    images_dir = output / "images"
    images_dir.mkdir(parents=True, exist_ok=True)
    catalog = []

    im_reader = PdfReader(im_pdf)
    with pdfplumber.open(im_pdf) as im_text:
        for page, image_index, name, category, unit in IM_PRODUCTS:
            code = f"IM-2026-P{page:02d}"
            filename = image_name(code)
            image = im_reader.pages[page - 1].images[image_index - 1].image
            save_product_image(image, images_dir / filename, "white")
            description = compact_text(im_text.pages[page - 1].extract_text(), name)
            catalog.append({
                "codigo": code,
                "nome": name,
                "descricao": description,
                "marca": "Integralmédica",
                "unidade": unit,
                "categoria": category,
                "subcategoria": "Catálogo Integralmédica 2026",
                "objetivos": objectives(category),
                "imagemResource": f"catalogos/im-darkness/images/{filename}",
                "imagemNome": filename,
            })

    darkness_reader = PdfReader(darkness_pdf)
    for page, name, category, unit, description in DARKNESS_PRODUCTS:
        code = f"DARK-2026-P{page:02d}"
        filename = image_name(code)
        page_image = darkness_reader.pages[page - 1].images[0].image.convert("RGB")
        width, height = page_image.size
        # O produto ocupa a faixa central/direita; a margem extra evita cortar potes largos.
        crop = page_image.crop((int(width * .35), int(height * .12), int(width * .97), int(height * .72)))
        crop = ImageOps.autocontrast(crop, cutoff=1)
        save_product_image(crop, images_dir / filename, "#111111")
        catalog.append({
            "codigo": code,
            "nome": name,
            "descricao": description + " Consulte a embalagem para composição, modo de uso e alergênicos.",
            "marca": "Darkness",
            "unidade": unit,
            "categoria": category,
            "subcategoria": "Catálogo Darkness 2026",
            "objetivos": objectives(category),
            "imagemResource": f"catalogos/im-darkness/images/{filename}",
            "imagemNome": filename,
        })

    catalog_file = output / "catalogo-im-darkness.json"
    catalog_file.write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest = {
        "produtos": len(catalog),
        "integralmedica": len(IM_PRODUCTS),
        "darkness": len(DARKNESS_PRODUCTS),
        "sha256Catalogo": hashlib.sha256(catalog_file.read_bytes()).hexdigest(),
    }
    (output / "manifesto.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(manifest, indent=2))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--im-pdf", type=Path, required=True)
    parser.add_argument("--darkness-pdf", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    build(args.im_pdf, args.darkness_pdf, args.output)


if __name__ == "__main__":
    main()
