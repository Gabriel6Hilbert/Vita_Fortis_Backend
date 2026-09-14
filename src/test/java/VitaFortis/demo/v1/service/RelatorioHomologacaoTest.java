package VitaFortis.demo.v1.service;

import VitaFortis.demo.v1.entity.Produto;
import VitaFortis.demo.v1.enums.CategoriaProduto;
import VitaFortis.demo.v1.enums.TipoRelatorio;
import VitaFortis.demo.v1.repository.*;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelatorioHomologacaoTest {
    @Test
    void exportacaoPreservaCelulasEPaginaAteOUltimoRegistro() throws Exception {
        var produtos=mock(ProdutoRepository.class);
        var lista=new ArrayList<Produto>();
        for(int i=0;i<120;i++) {
            var p=new Produto(); p.setCodigo("SKU-"+i); p.setNome(i==0 ? "Nome; com \"aspas\"\ne quebra" : "Produto "+i);
            p.setCategoria(CategoriaProduto.PROTEINAS); p.setPreco(null); lista.add(p);
        }
        when(produtos.findAll()).thenReturn(lista);
        var service=new RelatorioService(mock(PedidoRepository.class),produtos,mock(UsuarioRepository.class),mock(CupomRepository.class),mock(MovimentoCashbackRepository.class));
        LocalDate dia=LocalDate.of(2026,9,14);
        try(var workbook=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new ByteArrayInputStream(service.gerarXlsx(TipoRelatorio.PRODUTOS,dia,dia)))) {
            var sheet=workbook.getSheetAt(0);
            assertEquals(121,sheet.getPhysicalNumberOfRows());
            assertEquals(lista.get(0).getNome(),sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("SKU-119",sheet.getRow(120).getCell(0).getStringCellValue());
        }
        try(var document=org.apache.pdfbox.Loader.loadPDF(service.gerarPdf(TipoRelatorio.PRODUTOS,dia,dia))) {
            assertTrue(document.getNumberOfPages()>=3);
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(document);
            assertTrue(text.contains("SKU-119"));
            assertTrue(text.contains("Nome; com"));
        }
    }
}
