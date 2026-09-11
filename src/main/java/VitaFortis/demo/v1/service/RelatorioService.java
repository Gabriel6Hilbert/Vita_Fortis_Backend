package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Pedido;
import VitaFortis.demo.v1.enums.TipoRelatorio;
import VitaFortis.demo.v1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioService {
    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;
    private final CupomRepository cupons;
    private final MovimentoCashbackRepository cashback;

    public RelatorioService(PedidoRepository pedidos, ProdutoRepository produtos, UsuarioRepository usuarios,
                            CupomRepository cupons, MovimentoCashbackRepository cashback) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.usuarios = usuarios;
        this.cupons = cupons;
        this.cashback = cashback;
    }

    @Transactional(readOnly = true)
    public byte[] gerar(TipoRelatorio tipo, LocalDate inicio, LocalDate fim) {
        String csv = switch (tipo) {
            case PEDIDOS -> pedidos(inicio, fim);
            case PRODUTOS -> produtos();
            case CLIENTES -> clientes();
            case CUPONS -> cupons();
            case CASHBACK -> cashback();
        };
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] gerarXlsx(TipoRelatorio tipo, LocalDate inicio, LocalDate fim) {
        String csv = new String(gerar(tipo, inicio, fim), StandardCharsets.UTF_8).replace("\uFEFF", "");
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); var output = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(tipo.name()); int rowIndex = 0;
            for (String line : csv.split("\\R")) { var row=sheet.createRow(rowIndex++); String[] values=line.split(";",-1); for(int i=0;i<values.length;i++) row.createCell(i).setCellValue(values[i].replaceAll("^\\\"|\\\"$", "").replace("\\\"\\\"", "\\\"")); }
            if(rowIndex>0) for(int i=0;i<sheet.getRow(0).getLastCellNum();i++) sheet.autoSizeColumn(i);
            workbook.write(output); return output.toByteArray();
        } catch (java.io.IOException e) { throw new IllegalStateException("Nao foi possivel gerar XLSX", e); }
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(TipoRelatorio tipo, LocalDate inicio, LocalDate fim) {
        String csv = new String(gerar(tipo, inicio, fim), StandardCharsets.UTF_8).replace("\uFEFF", "");
        try (var document=new org.apache.pdfbox.pdmodel.PDDocument(); var output=new java.io.ByteArrayOutputStream()) {
            var a4=org.apache.pdfbox.pdmodel.common.PDRectangle.A4;var page=new org.apache.pdfbox.pdmodel.PDPage(new org.apache.pdfbox.pdmodel.common.PDRectangle(a4.getHeight(),a4.getWidth()));document.addPage(page);
            try(var content=new org.apache.pdfbox.pdmodel.PDPageContentStream(document,page)){content.beginText();content.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA),8);content.newLineAtOffset(28,page.getMediaBox().getHeight()-30);content.showText("Vita Fortis - "+tipo+" - "+inicio+" a "+fim);content.newLineAtOffset(0,-14);for(String line:csv.split("\\R")){String printable=line.replace(';',' ').replace('"',' ');content.showText(printable.substring(0,Math.min(150,printable.length())));content.newLineAtOffset(0,-11);}content.endText();}
            document.save(output);return output.toByteArray();
        } catch(java.io.IOException e){throw new IllegalStateException("Nao foi possivel gerar PDF",e);}
    }

    private String pedidos(LocalDate inicio, LocalDate fim) {
        LocalDateTime de = inicio.atStartOfDay();
        LocalDateTime ate = fim.atTime(LocalTime.MAX);
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"pedido", "data", "cliente", "status", "recebimento", "pagamento", "subtotal", "desconto", "frete", "total"});
        for (Pedido pedido : pedidos.findByDataPedidoBetween(de, ate)) {
            linhas.add(new String[]{String.valueOf(pedido.getId()), String.valueOf(pedido.getDataPedido()),
                    pedido.getUsuario().getEmail(), pedido.getStatus().name(), pedido.getFormaRecebimento().name(),
                    pedido.getStatusPagamento().name(), pedido.getSubtotal().toPlainString(),
                    pedido.getDesconto().toPlainString(), pedido.getFrete().toPlainString(), pedido.getTotal().toPlainString()});
        }
        return csv(linhas);
    }

    private String produtos() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"sku", "nome", "categoria", "preco", "estoque", "ativo", "total_vendido"});
        produtos.findAll().forEach(produto -> linhas.add(new String[]{produto.getCodigo(), produto.getNome(),
                produto.getCategoria().name(), produto.getPreco().toPlainString(),
                String.valueOf(produto.getQuantidadeEstoque()), String.valueOf(produto.isAtivo()),
                String.valueOf(produto.getTotalVendido())}));
        return csv(linhas);
    }

    private String clientes() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"id", "nome", "email", "telefone", "perfil", "ativo"});
        usuarios.findAll().forEach(usuario -> linhas.add(new String[]{String.valueOf(usuario.getId()), usuario.getNome(),
                usuario.getEmail(), usuario.getTelefone(), usuario.getTipo().name(), String.valueOf(usuario.isAtivo())}));
        return csv(linhas);
    }

    private String cupons() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"codigo", "tipo", "desconto", "ativo", "colaborador", "percentual_cashback"});
        cupons.findAll().forEach(cupom -> linhas.add(new String[]{cupom.getCodigo(), cupom.getTipo().name(),
                cupom.getDesconto().toPlainString(), String.valueOf(cupom.isAtivo()),
                cupom.getColaborador() == null ? null : cupom.getColaborador().getEmail(),
                cupom.getPercentualCashback() == null ? null : cupom.getPercentualCashback().toPlainString()}));
        return csv(linhas);
    }

    private String cashback() {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"data", "colaborador", "tipo", "valor", "saldo_anterior", "saldo_novo", "responsavel", "justificativa"});
        cashback.findAll().forEach(movimento -> linhas.add(new String[]{String.valueOf(movimento.getCriadoEm()),
                movimento.getColaborador().getEmail(), movimento.getTipo().name(), movimento.getValor().toPlainString(),
                movimento.getSaldoAnterior().toPlainString(), movimento.getSaldoNovo().toPlainString(),
                movimento.getResponsavel(), movimento.getJustificativa()}));
        return csv(linhas);
    }

    private String csv(List<String[]> linhas) {
        return linhas.stream().map(linha -> java.util.Arrays.stream(linha)
                        .map(this::escapar).collect(java.util.stream.Collectors.joining(";")))
                .collect(java.util.stream.Collectors.joining("\r\n"));
    }

    private String escapar(String valor) {
        String seguro = valor == null ? "" : valor.replace("\"", "\"\"");
        return "\"" + seguro + "\"";
    }
}
