from pathlib import Path

from docx import Document
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor


OUTPUT = Path(__file__).resolve().parents[1] / "docs" / "legal" / "Pacote_de_Aprovacao_Juridica_Vita_Fortis.docx"


def shade(cell, color):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:fill"), color)
    tc_pr.append(shd)


def borders(table):
    tbl_pr = table._tbl.tblPr
    tbl_borders = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        el = OxmlElement(f"w:{edge}")
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), "4")
        el.set(qn("w:color"), "D9D9D9")
        tbl_borders.append(el)
    tbl_pr.append(tbl_borders)


def set_cell_margin(cell, top=110, start=120, bottom=110, end=120):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for name, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = OxmlElement(f"w:{name}")
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")
        tc_mar.append(node)


def repeat_header(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def add_table(doc, headers, rows, widths):
    table = doc.add_table(rows=1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    borders(table)
    hdr = table.rows[0]
    repeat_header(hdr)
    for i, text in enumerate(headers):
        cell = hdr.cells[i]
        cell.width = Cm(widths[i])
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        shade(cell, "173B2C")
        set_cell_margin(cell)
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(text)
        run.bold = True
        run.font.color.rgb = RGBColor(255, 255, 255)
        run.font.size = Pt(9)
    for r_index, row in enumerate(rows):
        cells = table.add_row().cells
        for i, text in enumerate(row):
            cell = cells[i]
            cell.width = Cm(widths[i])
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            set_cell_margin(cell)
            if r_index % 2:
                shade(cell, "F3F7F5")
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            run = p.add_run(text)
            run.font.size = Pt(8.5)
    doc.add_paragraph().paragraph_format.space_after = Pt(0)
    return table


def bullet(doc, text, level=0):
    p = doc.add_paragraph(style="List Bullet" if level == 0 else "List Bullet 2")
    p.add_run(text)
    return p


def numbered(doc, text):
    p = doc.add_paragraph(style="List Number")
    p.add_run(text)
    return p


def checkbox(doc, text):
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Cm(0.25)
    p.add_run("[  ]  ").bold = True
    p.add_run(text)
    return p


doc = Document()
section = doc.sections[0]
section.top_margin = Cm(1.8)
section.bottom_margin = Cm(1.7)
section.left_margin = Cm(2.0)
section.right_margin = Cm(2.0)

styles = doc.styles
styles["Normal"].font.name = "Aptos"
styles["Normal"].font.size = Pt(10.5)
styles["Normal"].paragraph_format.space_after = Pt(6)
styles["Normal"].paragraph_format.line_spacing = 1.08
for name, size in (("Title", 24), ("Heading 1", 16), ("Heading 2", 12)):
    style = styles[name]
    style.font.name = "Aptos Display"
    style.font.size = Pt(size)
    style.font.color.rgb = RGBColor(0, 0, 0)
    style.font.bold = True
    style.paragraph_format.space_before = Pt(12 if name != "Title" else 0)
    style.paragraph_format.space_after = Pt(6)

title = doc.add_paragraph(style="Title")
title.add_run("Pacote de Aprovação Jurídica e Retenção de Dados")
title_pr = title._p.get_or_add_pPr()
title_borders = OxmlElement("w:pBdr")
title_bottom = OxmlElement("w:bottom")
title_bottom.set(qn("w:val"), "nil")
title_borders.append(title_bottom)
title_pr.append(title_borders)
subtitle = doc.add_paragraph()
subtitle.paragraph_format.space_after = Pt(14)
r = subtitle.add_run("Vita Fortis  |  Documento para revisão e decisão formal")
r.bold = True
r.font.color.rgb = RGBColor(23, 59, 44)

doc.add_paragraph(
    "Este documento organiza o que a empresa e o profissional jurídico precisam revisar para aprovar "
    "as políticas do site e os prazos de retenção. Ele não é um parecer jurídico e não substitui a "
    "validação de advogado ou responsável legal habilitado. A aprovação somente estará concluída "
    "depois que os fatos forem confirmados, os textos forem revisados e a decisão for registrada."
)

doc.add_heading("Identificação da empresa", level=1)
add_table(doc, ["Campo", "Informação"], [
    ("Nome empresarial", "61.156.510 Myllena dos Santos Pereira de Freitas"),
    ("Nome fantasia", "Vita Fortis"),
    ("CNPJ", "61.156.510/0001-47"),
    ("Endereço", "Avenida Waldyr Soeiro Emrich, 5230, Bairro Diamante, Belo Horizonte/MG, CEP 30660-560"),
    ("Canal de atendimento e privacidade", "vitafortis@atendimentos.com.br"),
    ("Responsável informado pelo canal", "Vita Fortis"),
], [5.0, 11.2])

doc.add_heading("Como será feita a aprovação", level=1)
for text in [
    "A Vita Fortis confirma se os dados da empresa, formas de atendimento, entrega própria, pagamentos, fornecedores e operação descritos nas minutas correspondem à realidade.",
    "O advogado revisa a Política de Privacidade, Política de Cookies, Termos de Uso e Compra, Política de Entrega e Retirada, Política de Trocas e Devoluções e o Inventário LGPD.",
    "O advogado confirma ou corrige as bases legais, os prazos de retenção, o tratamento de dados de menores, os compartilhamentos e as regras de consumo.",
    "As correções são incorporadas aos textos e recebem número de versão e data de vigência.",
    "A empresa e o revisor jurídico registram a aprovação na seção final deste documento.",
    "Somente depois disso as minutas deixam de ser identificadas como pendentes e a versão aprovada pode ser publicada no site.",
]:
    numbered(doc, text)

doc.add_heading("Documentos que devem ser revisados", level=1)
for text in [
    "Política de Privacidade",
    "Política de Cookies",
    "Termos de Uso e Compra",
    "Política de Entrega e Retirada",
    "Política de Trocas, Devoluções e Arrependimento",
    "Inventário LGPD com finalidades, bases legais, compartilhamentos e retenção",
]:
    checkbox(doc, text)

doc.add_heading("Proposta de retenção para decisão jurídica", level=1)
doc.add_paragraph(
    "Os prazos abaixo são uma proposta operacional. O advogado deve aprovar, alterar ou indicar a "
    "fundamentação aplicável. Havendo obrigação legal, processo, fraude ou exercício regular de direitos, "
    "o dado poderá precisar ser conservado por prazo maior e com acesso restrito."
)
retention_rows = [
    ("Conta e cadastro", "Enquanto ativa; após encerramento, eliminar ou anonimizar dados sem obrigação em até 12 meses", "[  ] Aprovar  [  ] Alterar"),
    ("Endereços", "Avulsos até o encerramento da conta; os ligados a pedidos seguem o prazo do pedido", "[  ] Aprovar  [  ] Alterar"),
    ("Sacola e favoritos", "Até 12 meses de inatividade ou exclusão pelo usuário", "[  ] Aprovar  [  ] Alterar"),
    ("Pedidos e referências de pagamento", "5 anos após conclusão ou cancelamento, salvo prazo aplicável maior", "[  ] Aprovar  [  ] Alterar"),
    ("Comunicações transacionais", "Mesmo prazo do pedido relacionado", "[  ] Aprovar  [  ] Alterar"),
    ("Comunicações promocionais", "Até revogação; bloqueio imediato e prova da escolha por até 5 anos", "[  ] Aprovar  [  ] Alterar"),
    ("Recuperação de senha", "Token por 30 minutos; exclusão automática até 24 horas após expiração", "[  ] Aprovar  [  ] Alterar"),
    ("Solicitações LGPD e auditoria", "5 anos após encerramento", "[  ] Aprovar  [  ] Alterar"),
    ("Sessão e registros de acesso", "Sessão até logout ou expiração; registros coletados por 6 meses", "[  ] Aprovar  [  ] Alterar"),
    ("Avaliações", "Enquanto publicadas ou até retirada; evidência de moderação por até 5 anos quando necessária", "[  ] Aprovar  [  ] Alterar"),
    ("Backups", "7 cópias diárias e 4 semanais, com eliminação segura ao final da rotação", "[  ] Aprovar  [  ] Alterar"),
]
add_table(doc, ["Categoria", "Prazo proposto", "Decisão"], retention_rows, [4.0, 8.1, 4.1])

doc.add_heading("Decisões obrigatórias antes da publicação", level=1)
for text in [
    "Confirmar as bases legais de cada tratamento descrito no inventário.",
    "Confirmar os fornecedores que efetivamente receberão dados, inclusive hospedagem, pagamento e e-mail.",
    "Avaliar transferência internacional de dados pelos fornecedores contratados.",
    "Definir como será obtida e registrada a autorização do responsável quando menores realizarem pedidos.",
    "Manter Google Analytics, Meta Pixel e cookies de publicidade desativados até decisão específica e atualização das políticas.",
    "Confirmar as regras de troca voluntária sem limitar arrependimento, defeito, divergência ou demais direitos legais.",
    "Confirmar que o canal de privacidade e os dados empresariais estarão visíveis e atualizados.",
]:
    checkbox(doc, text)

doc.add_heading("Pontos já definidos para conferência", level=1)
for text in [
    "A entrega será realizada pela própria Vita Fortis, sem transportadora externa.",
    "O acompanhamento operacional poderá ser feito pelo WhatsApp cadastrado.",
    "O frete gratuito aplica-se somente quando o valor dos produtos, após descontos, for superior a R$ 200,00.",
    "Pedidos de menores somente devem ocorrer com conhecimento e autorização dos pais ou responsáveis, sujeito à definição do mecanismo jurídico.",
    "Analytics, Meta Pixel e publicidade comportamental permanecem em espera.",
]:
    bullet(doc, text)

doc.add_heading("Registro da revisão jurídica", level=1)
add_table(doc, ["Item", "Preenchimento"], [
    ("Nome do advogado ou responsável jurídico", "____________________________________________________________"),
    ("OAB ou identificação profissional quando aplicável", "____________________________________________________________"),
    ("Data da revisão", "____ / ____ / ________"),
    ("Versão aprovada", "________________________"),
    ("Resultado", "[  ] Aprovado  [  ] Aprovado com ajustes  [  ] Reprovado"),
    ("Observações e ajustes obrigatórios", "____________________________________________________________\n____________________________________________________________"),
], [6.8, 9.4])

doc.add_heading("Declaração de aprovação", level=1)
doc.add_paragraph(
    "Após a incorporação dos ajustes registrados, as partes abaixo confirmam que a versão indicada foi "
    "revisada para publicação. Esta assinatura deve identificar claramente a versão aprovada e não alcança "
    "alterações posteriores realizadas sem nova revisão."
)
doc.add_paragraph("\n________________________________________        ________________________________________")
doc.add_paragraph("Responsável pela Vita Fortis                         Responsável pela revisão jurídica")
doc.add_paragraph("Data: ____ / ____ / ________                         Data: ____ / ____ / ________")

doc.add_heading("Referências oficiais para o revisor", level=1)
refs = [
    ("Lei Geral de Proteção de Dados Pessoais", "https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm"),
    ("Direitos dos titulares na ANPD", "https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados-1/direito-dos-titulares"),
    ("Código de Defesa do Consumidor", "https://www.planalto.gov.br/ccivil_03/leis/l8078compilado.htm"),
    ("Decreto do Comércio Eletrônico", "https://www.planalto.gov.br/ccivil_03/_ato2011-2014/2013/decreto/d7962.htm"),
]
for label, url in refs:
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(2)
    label_run = p.add_run(label + ": ")
    label_run.bold = True
    label_run.font.size = Pt(8.5)
    url_run = p.add_run(url)
    url_run.font.size = Pt(8.5)

footer = section.footer.paragraphs[0]
footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
run = footer.add_run("Vita Fortis  |  Pacote para aprovação jurídica")
run.font.size = Pt(8)
run.font.color.rgb = RGBColor(90, 90, 90)

OUTPUT.parent.mkdir(parents=True, exist_ok=True)
doc.save(OUTPUT)
print(OUTPUT)
