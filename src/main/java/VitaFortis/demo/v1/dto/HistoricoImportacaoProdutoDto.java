package VitaFortis.demo.v1.dto;

import VitaFortis.demo.v1.entity.HistoricoImportacaoProduto;
import java.time.LocalDateTime;

public record HistoricoImportacaoProdutoDto(Long id, String nomeArquivo, String hashArquivo,
        String responsavel, LocalDateTime criadoEm, String resultado, int inseridos,
        int atualizados, int rejeitados, String detalhes) {
    public static HistoricoImportacaoProdutoDto from(HistoricoImportacaoProduto h) {
        return new HistoricoImportacaoProdutoDto(h.getId(), h.getNomeArquivo(), h.getHashArquivo(),
                h.getResponsavel(), h.getCriadoEm(), h.getResultado(), h.getInseridos(),
                h.getAtualizados(), h.getRejeitados(), h.getDetalhes());
    }
}
