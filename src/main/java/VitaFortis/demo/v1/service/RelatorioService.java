package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Pedido;
import VitaFortis.demo.v1.dto.RelatorioFiltroDto;
import VitaFortis.demo.v1.enums.StatusPagamento;
import VitaFortis.demo.v1.enums.TipoUsuario;
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
        return gerar(tipo, RelatorioFiltroDto.periodo(inicio,fim));
    }

    @Transactional(readOnly = true)
    public byte[] gerar(TipoRelatorio tipo, RelatorioFiltroDto filtro) {
        filtro.validar();
        String csv = switch (tipo) {
            case VENDAS -> pedidos(filtro,true);
            case PEDIDOS -> pedidos(filtro,false);
            case PRODUTOS -> produtos(filtro);
            case CLIENTES -> clientes(filtro);
            case CUPONS -> cupons(filtro);
            case CASHBACK -> cashback(filtro);
        };
        return ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] gerarXlsx(TipoRelatorio tipo, LocalDate inicio, LocalDate fim) {
        return gerarXlsx(tipo, RelatorioFiltroDto.periodo(inicio,fim));
    }

    @Transactional(readOnly = true)
    public byte[] gerarXlsx(TipoRelatorio tipo, RelatorioFiltroDto filtro) {
        LocalDate inicio=filtro.inicio(), fim=filtro.fim();
        String csv = new String(gerar(tipo, filtro), StandardCharsets.UTF_8).replace("\uFEFF", "");
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); var output = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(tipo.name()); int rowIndex = 0;
            for (var values : CsvDados.ler(csv, ';')) {
                var row=sheet.createRow(rowIndex++);
                for(int i=0;i<values.size();i++) row.createCell(i).setCellValue(values.get(i));
            }
            if(rowIndex>0) for(int i=0;i<sheet.getRow(0).getLastCellNum();i++) sheet.autoSizeColumn(i);
            workbook.write(output); return output.toByteArray();
        } catch (java.io.IOException e) { throw new IllegalStateException("Nao foi possivel gerar XLSX", e); }
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(TipoRelatorio tipo, LocalDate inicio, LocalDate fim) {
        return gerarPdf(tipo, RelatorioFiltroDto.periodo(inicio,fim));
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdf(TipoRelatorio tipo, RelatorioFiltroDto filtro) {
        LocalDate inicio=filtro.inicio(), fim=filtro.fim();
        String csv = new String(gerar(tipo, filtro), StandardCharsets.UTF_8).replace("\uFEFF", "");
        try (var document=new org.apache.pdfbox.pdmodel.PDDocument(); var output=new java.io.ByteArrayOutputStream()) {
            var font=new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.COURIER);
            var lines=new ArrayList<String>();
            for (var row : CsvDados.ler(csv, ';')) {
                String text=String.join(" | ",row).replace('\n',' ').replace('\r',' ');
                var safe=new StringBuilder();
                for(int cp:text.codePoints().toArray()) {
                    String ch=new String(Character.toChars(cp));
                    try { font.encode(ch); safe.append(ch); } catch(IllegalArgumentException e) { safe.append('?'); }
                }
                String printable=safe.toString();
                if(printable.isEmpty()) lines.add("");
                for(int offset=0;offset<printable.length();offset+=150) lines.add(printable.substring(offset,Math.min(offset+150,printable.length())));
            }
            if(lines.isEmpty()) lines.add("Sem registros");
            for(int start=0;start<lines.size();start+=45) {
                var a4=org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
                var page=new org.apache.pdfbox.pdmodel.PDPage(new org.apache.pdfbox.pdmodel.common.PDRectangle(a4.getHeight(),a4.getWidth()));
                document.addPage(page);
                try(var content=new org.apache.pdfbox.pdmodel.PDPageContentStream(document,page)) {
                    content.beginText(); content.setFont(font,8); content.newLineAtOffset(28,page.getMediaBox().getHeight()-30);
                    content.showText("Vita Fortis - "+tipo+" - "+inicio+" a "+fim+" - Pagina "+document.getNumberOfPages());
                    content.newLineAtOffset(0,-18);
                    for(int i=start;i<Math.min(start+45,lines.size());i++) { content.showText(lines.get(i)); content.newLineAtOffset(0,-11); }
                    content.endText();
                }
            }
            document.save(output);return output.toByteArray();
        } catch(java.io.IOException e){throw new IllegalStateException("Nao foi possivel gerar PDF",e);}
    }

    private String pedidos(RelatorioFiltroDto filtro, boolean somenteVendas) {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"pedido", "data", "cliente", "status", "recebimento", "forma_pagamento", "status_pagamento", "subtotal", "desconto", "frete", "total"});
        for (Pedido pedido : pedidosFiltrados(filtro)) {
            if(somenteVendas && pedido.getStatusPagamento()!=StatusPagamento.APROVADO) continue;
            linhas.add(new String[]{String.valueOf(pedido.getId()), String.valueOf(pedido.getDataPedido()),
                    pedido.getUsuario().getEmail(), pedido.getStatus().name(), pedido.getFormaRecebimento().name(),
                    pedido.getFormaPagamento(), pedido.getStatusPagamento().name(), pedido.getSubtotal().toPlainString(),
                    pedido.getDesconto().toPlainString(), pedido.getFrete().toPlainString(), pedido.getTotal().toPlainString()});
        }
        return csv(linhas);
    }

    private String produtos(RelatorioFiltroDto filtro) {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"sku", "nome", "categoria", "preco", "estoque", "ativo", "total_vendido"});
        produtos.findAll().stream().filter(p -> filtro.contem(p.getCodigo(),p.getNome(),p.getMarca()) && (filtro.ativo()==null || p.isAtivo()==filtro.ativo()) && (filtro.categoria()==null || p.getCategoria().equals(filtro.categoria())) && (filtro.estoqueMax()==null || p.getQuantidadeEstoque()<=filtro.estoqueMax())).forEach(produto -> linhas.add(new String[]{produto.getCodigo(), produto.getNome(),
                produto.getCategoria(), (produto.getPreco() == null ? "" : produto.getPreco().toPlainString()),
                String.valueOf(produto.getQuantidadeEstoque()), String.valueOf(produto.isAtivo()),
                String.valueOf(produto.getTotalVendido())}));
        return csv(linhas);
    }

    private String clientes(RelatorioFiltroDto filtro) {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"id", "nome", "email", "telefone", "perfil", "ativo"});
        usuarios.findAll().stream().filter(u -> u.getTipo()==TipoUsuario.CLIENTE && filtro.contem(u.getNome(),u.getEmail(),u.getTelefone()) && (filtro.ativo()==null || u.isAtivo()==filtro.ativo())).forEach(usuario -> linhas.add(new String[]{String.valueOf(usuario.getId()), usuario.getNome(),
                usuario.getEmail(), usuario.getTelefone(), usuario.getTipo().name(), String.valueOf(usuario.isAtivo())}));
        return csv(linhas);
    }

    private String cupons(RelatorioFiltroDto filtro) {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"codigo", "tipo", "desconto", "ativo", "colaborador", "percentual_cashback", "usos_confirmados_periodo", "vendas_confirmadas_periodo", "descontos_periodo"});
        var vendas=pedidos.findByDataPedidoBetween(filtro.inicio().atStartOfDay(),filtro.fim().atTime(LocalTime.MAX)).stream()
                .filter(p->p.getStatusPagamento()==StatusPagamento.APROVADO).toList();
        for(var cupom:cupons.findAll()) {
            if(!filtro.contem(cupom.getCodigo(),cupom.getDescricao()) || (filtro.ativo()!=null && cupom.isAtivo()!=filtro.ativo())) continue;
            if(filtro.colaboradorId()!=null && (cupom.getColaborador()==null || !filtro.colaboradorId().equals(cupom.getColaborador().getId()))) continue;
            var usos=vendas.stream().filter(p->p.getCupomUtilizado()!=null && p.getCupomUtilizado().getId().equals(cupom.getId())).toList();
            linhas.add(new String[]{cupom.getCodigo(),cupom.getTipo().name(),cupom.getDesconto().toPlainString(),String.valueOf(cupom.isAtivo()),
                    cupom.getColaborador()==null ? "" : cupom.getColaborador().getEmail(),String.valueOf(cupom.getPercentualCashback()),String.valueOf(usos.size()),
                    usos.stream().map(Pedido::getTotal).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add).toPlainString(),
                    usos.stream().map(Pedido::getDesconto).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add).toPlainString()});
        }
        return csv(linhas);
    }

    private String cashback(RelatorioFiltroDto filtro) {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(new String[]{"data", "colaborador", "tipo", "valor", "saldo_anterior", "saldo_novo", "responsavel", "justificativa"});
        cashback.findAll().stream().filter(m -> !m.getCriadoEm().isBefore(filtro.inicio().atStartOfDay()) && m.getCriadoEm().isBefore(filtro.fim().plusDays(1).atStartOfDay()) && (filtro.colaboradorId()==null || filtro.colaboradorId().equals(m.getColaborador().getId())) && filtro.contem(m.getColaborador().getNome(),m.getColaborador().getEmail(),m.getJustificativa())).forEach(movimento -> linhas.add(new String[]{String.valueOf(movimento.getCriadoEm()),
                movimento.getColaborador().getEmail(), movimento.getTipo().name(), movimento.getValor().toPlainString(),
                movimento.getSaldoAnterior().toPlainString(), movimento.getSaldoNovo().toPlainString(),
                movimento.getResponsavel(), movimento.getJustificativa()}));
        return csv(linhas);
    }

    private List<Pedido> pedidosFiltrados(RelatorioFiltroDto filtro) {
        return pedidos.findByDataPedidoBetween(filtro.inicio().atStartOfDay(),filtro.fim().atTime(LocalTime.MAX)).stream()
                .filter(p->filtro.contem(String.valueOf(p.getId()),p.getUsuario().getNome(),p.getUsuario().getEmail()))
                .filter(p->filtro.status()==null || p.getStatus()==filtro.status())
                .filter(p->filtro.recebimento()==null || p.getFormaRecebimento()==filtro.recebimento())
                .filter(p->filtro.pagamento()==null || p.getFormaPagamento().equals(filtro.pagamento()))
                .toList();
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
